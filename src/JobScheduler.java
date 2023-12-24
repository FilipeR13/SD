import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JobScheduler implements Runnable{

    private Server server;
    private final Lock lock = new ReentrantLock();
    private final Condition conn = lock.newCondition();

    public JobScheduler(Server server) {
        this.server = server;
    }

    public void run() {
        while (true) {
            lock.lock();
            try {
                if(!server.getPendingPrograms().isEmpty()) {

                    ProgramRequest programRequest = server.getPendingPrograms().peek();

                    // Find a worker with enough memory, wait if none are available
                    List<Worker> availableWorkers = findAvailableWorker(programRequest.getMemory());
                    while (availableWorkers.isEmpty()) {
                        conn.await();
                        availableWorkers = findAvailableWorker(programRequest.getMemory());
                        programRequest = server.getPendingPrograms().peek();
                    }

                    // Send the program to the best worker
                    sendJobToBestWorker(programRequest, availableWorkers);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }

    private List<Worker> findAvailableWorker(int requiredMemory) {
        return server.getConnectedWorkers().values().stream()
                        .filter(worker -> worker.getMemory_available() >= requiredMemory)
                        .toList();
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

    public void setCondition() {
        this.lock.lock();
        try {
            this.conn.signal();
        } finally {
            this.lock.unlock();
        }
    }
}

