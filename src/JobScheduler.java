import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JobScheduler implements Runnable{

    private Server server;
    private final Lock lock = new ReentrantLock();

    public JobScheduler(Server server) {
        this.server = server;
    }

    public void run() {
        while (true) {
            lock.lock();
            try {
                if(!server.getPendingPrograms().isEmpty()) {

                    //check if all the workers have 0 memory available
                    boolean allWorkersFull = true;
                    for (Worker worker : server.getConnectedWorkers()) {
                        if (worker.getMemory_available() != 0) {
                            allWorkersFull = false;
                        }
                    }
                    if (!allWorkersFull) {

                        ProgramRequest programRequest = server.getPendingPrograms().peek();

                        // Find a worker with enough memory, wait if none are available
                        List<Worker> availableWorkers = findAvailableWorker(programRequest.getMemory());
                        while (availableWorkers.size() == 0) {
                            availableWorkers = findAvailableWorker(programRequest.getMemory());
                        }

                        // Send the program to the best worker
                        sendJobToBestWorker(programRequest, availableWorkers);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    private List<Worker> findAvailableWorker(int requiredMemory) {
        List<Worker> workers = new ArrayList<>();
        for (Worker worker : server.getConnectedWorkers()) {
            if (worker.getMemory_available() >= requiredMemory) {
                workers.add(worker);
            }
        }
        return workers;
    }

    private void sendJobToBestWorker(ProgramRequest pr, List<Worker> availableWorkers) throws IOException {
        // go through all the workers and find the one with the least amount of jobs being executed
        // if there are more than one worker with the same amount of jobs being executed, choose the one with the most memory available

        Worker bestWorker = availableWorkers.get(0);
        for (Worker worker : availableWorkers) {
            if (worker.getNum_jobs() < bestWorker.getNum_jobs()) {
                bestWorker = worker;
            } else if (worker.getNum_jobs() == bestWorker.getNum_jobs()) {
                if (worker.getMemory_available() > bestWorker.getMemory_available()) {
                    bestWorker = worker;
                }
            }
        }

        // send the program to the best worker

        SafeDataOutputStream bestWorkerOut = bestWorker.getOut();
        Message.serialize(bestWorkerOut,"SEND_PROGRAM",pr.getClientUsername() + ";" + pr.getPedido_id() + ";" + pr.getMemory() + ";" + new String(pr.getFile()));
        bestWorkerOut.flush();

        ProgramRequest pr2 = this.server.getPendingPrograms().poll();
        bestWorker.setMemory_available(bestWorker.getMemory_available() - pr.getMemory());
        bestWorker.setNum_jobs(bestWorker.getNum_jobs() + 1);

    }
}

