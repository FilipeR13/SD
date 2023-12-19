import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private Map<String, Account> accounts;
    private Map<String, DataOutputStream> connectedClients;
    private static PriorityQueue<ProgramRequest> pendingPrograms;
    //Map for the connectedWorkers with an int as a key and then a tuple of ints for data
    private List<Worker> connectedWorkers;


    private final Lock accountsLock = new ReentrantLock();
    private final Lock connectedClientsLock = new ReentrantLock();
    private final Lock pendingProgramsLock = new ReentrantLock();
    private final Lock connectedWorkersLock = new ReentrantLock();


    public Server(){
        this.accounts = new HashMap<>();
        this.connectedClients = new HashMap<>();
        this.pendingPrograms = new PriorityQueue<>(new ProgramRequestComparator());
        this.connectedWorkers = new ArrayList<>();
    }

    // getters and setters

    public Map<String, Account> getAccounts() {
        return accounts;
    }

    public Map<String, DataOutputStream> getConnectedClients() {
        return connectedClients;
    }

    public PriorityQueue<ProgramRequest> getPendingPrograms() {
        return pendingPrograms;
    }

    public List<Worker> getConnectedWorkers() {
        return connectedWorkers;
    }

    public void setAccounts(Map<String, Account> accounts) {
        this.accounts = accounts;
    }

    public void setConnectedClients(Map<String, DataOutputStream> connectedClients) {
        this.connectedClients = connectedClients;
    }

   public void setPendingPrograms(PriorityQueue<ProgramRequest> pendingPrograms) {
            this.pendingPrograms = pendingPrograms;
    }

    public void setConnectedWorkers(List<Worker> connectedWorkers) {
        this.connectedWorkers = connectedWorkers;
    }

    // adds and removes from the structures

    public void addAccount(String username, Account account) {
        accountsLock.lock();
        try {
            this.accounts.put(username, account);
        } finally {
            accountsLock.unlock();
        }
    }

    public void removeAccount(String username) {
        accountsLock.lock();
        try {
            this.accounts.remove(username);
        } finally {
            accountsLock.unlock();
        }
    }

    public void addConnectedClient(String username, DataOutputStream out) {
        connectedClientsLock.lock();
        try {
            this.connectedClients.put(username, out);
        } finally {
            connectedClientsLock.unlock();
        }
    }

    public void removeConnectedClient(String username) {
        connectedClientsLock.lock();
        try {
            this.connectedClients.remove(username);
        } finally {
            connectedClientsLock.unlock();
        }
    }

    public void addPendingProgram(ProgramRequest pr) {
        pendingProgramsLock.lock();
        try {
            this.pendingPrograms.offer(pr);
        } finally {
            pendingProgramsLock.unlock();
        }
    }

    public void removePendingProgram() {
        pendingProgramsLock.lock();
        try {
            this.pendingPrograms.poll();
        } finally {
            pendingProgramsLock.unlock();
        }
    }

    public void addConnectedWorker(Worker worker){
        connectedWorkersLock.lock();
        try {
            this.connectedWorkers.add(worker);
        } finally {
            connectedWorkersLock.unlock();
        }
    }

    public void removeConnectedWorker(int worker_id){
        connectedWorkersLock.lock();
        try {
            for(Worker w : this.connectedWorkers){
                if(w.getWorker_id() == worker_id){
                    this.connectedWorkers.remove(w);
                    break;
                }
            }
        } finally {
            connectedWorkersLock.unlock();
        }
    }

    // get a specific element from the structures

    public Account getAccount(String username) {
        accountsLock.lock();
        try {
            return this.accounts.get(username);
        } finally {
            accountsLock.unlock();
        }
    }

    public DataOutputStream getConnectedClient(String username) {
        connectedClientsLock.lock();
        try {
            return this.connectedClients.get(username);
        } finally {
            connectedClientsLock.unlock();
        }
    }

    public ProgramRequest getPendingProgram() {
        pendingProgramsLock.lock();
        try {
            return this.pendingPrograms.peek();
        } finally {
            pendingProgramsLock.unlock();
        }
    }

    public Worker getConnectedWorker(int id) {
        connectedWorkersLock.lock();
        try {
            for (Worker w : this.connectedWorkers) {
                if (w.getWorker_id() == id) {
                    return w;
                }
            }
            return null;
        } finally {
            connectedWorkersLock.unlock();
        }
    }

    // Methods for confirming an element exists in the structures with locks

    public boolean containsAccount(String username) {
        accountsLock.lock();
        try {
            return this.accounts.containsKey(username);
        } finally {
            accountsLock.unlock();
        }
    }

    public boolean containsConnectedClient(String username) {
        connectedClientsLock.lock();
        try {
            return this.connectedClients.containsKey(username);
        } finally {
            connectedClientsLock.unlock();
        }
    }

    public boolean containsConnectedWorker(int id) {
        connectedWorkersLock.lock();
        try {
            for (Worker w : this.connectedWorkers) {
                if (w.getWorker_id() == id) {
                    return true;
                }
            }
            return false;
        } finally {
            connectedWorkersLock.unlock();
        }
    }

    // Methods for getting the size of the structures with locks

    public int sizeAccounts() {
        accountsLock.lock();
        try {
            return this.accounts.size();
        } finally {
            accountsLock.unlock();
        }
    }

    public int sizeConnectedClients() {
        connectedClientsLock.lock();
        try {
            return this.connectedClients.size();
        } finally {
            connectedClientsLock.unlock();
        }
    }

    public int sizePendingPrograms() {
        pendingProgramsLock.lock();
        try {
            return this.pendingPrograms.size();
        } finally {
            pendingProgramsLock.unlock();
        }
    }

    public int sizeConnectedWorkers() {
        connectedWorkersLock.lock();
        try {
            return this.connectedWorkers.size();
        } finally {
            connectedWorkersLock.unlock();
        }
    }

    // Methods for checking if the structures are empty with locks

    public boolean isEmptyAccounts() {
        accountsLock.lock();
        try {
            return this.accounts.isEmpty();
        } finally {
            accountsLock.unlock();
        }
    }

    public boolean isEmptyConnectedClients() {
        connectedClientsLock.lock();
        try {
            return this.connectedClients.isEmpty();
        } finally {
            connectedClientsLock.unlock();
        }
    }

    public boolean isEmptyPendingPrograms() {
        pendingProgramsLock.lock();
        try {
            return this.pendingPrograms.isEmpty();
        } finally {
            pendingProgramsLock.unlock();
        }
    }

    public boolean isEmptyConnectedWorkers() {
        connectedWorkersLock.lock();
        try {
            return this.connectedWorkers.isEmpty();
        } finally {
            connectedWorkersLock.unlock();
        }
    }
    public void changeMemoryWorkerPerId(int worker_id, int memory){
        connectedWorkersLock.lock();
        try {
            for (Worker w : this.connectedWorkers) {
                if (w.getWorker_id() == worker_id) {
                    w.setMemory_available(w.getMemory_available() + memory);
                }
            }
        } finally {
            connectedWorkersLock.unlock();
        }
    }

    // Methods for getting the element of the queue (poll) with locks

    public ProgramRequest pollPendingProgram() {
        pendingProgramsLock.lock();
        try {
            return this.pendingPrograms.poll();
        } finally {
            pendingProgramsLock.unlock();
        }
    }

    // Print of the queue pending programs

    public void printPendingPrograms() {
        pendingProgramsLock.lock();
        try {
            for (ProgramRequest pr : this.pendingPrograms) {
                System.out.println(pr.getPedido_id() + " " + pr.getMemory() + " " + pr.getClientUsername());
            }
        } finally {
            pendingProgramsLock.unlock();
        }
    }

    //method used to send a program to the best worker available (with more memory available)

    //when there isnt enough memory available and there is a program waiting to be executed and I send a server status, the server status is only sent after there is memory available,
    // the code is getting stuck inside a while(true) and does not send the server status back to the client

    public void sendProgram(ProgramRequest pr) throws IOException {

        while (true) {
            int bestMemoryAvailable = Integer.MIN_VALUE;
            Worker bestWorker = null;

            // Check available memory in connected workers
            for (Worker w : this.connectedWorkers) {
                if (w.getMemory_available() > bestMemoryAvailable && w.getMemory_available() >= pr.getMemory()) {
                    bestMemoryAvailable = w.getMemory_available();
                    bestWorker = w;
                }
            }

            if (bestWorker != null) {
                // Found a worker with enough memory, send the program
                DataOutputStream bestWorkerOut = bestWorker.getOut();
                Message.serialize(bestWorkerOut,"SEND_PROGRAM",pr.getClientUsername() + ";" + pr.getPedido_id() + ";" + pr.getMemory() + ";" + new String(pr.getFile()));
                bestWorkerOut.flush();

                this.getPendingPrograms().poll();
                bestWorker.setMemory_available(bestWorker.getMemory_available() - pr.getMemory());
                // Exit the loop since the program has been sent
                break;
            }
        }
    }

    // main method

    public static void main(String[] args) throws InterruptedException{
        int portClients = 9090;
        int portWorkers = 9091;
        int id_worker = 1;
        Server server = new Server();

        try {
            // Create server sockets
            ServerSocket serverSocketClients = new ServerSocket(portClients);
            System.out.println("Server is listening on port " + portClients + " for clients");

            ServerSocket serverSocketWorkers = new ServerSocket(portWorkers);
            System.out.println("Server is listening on port " + portWorkers + " for workers");

            // Create separate threads for handling clients and workers
            Thread clientAcceptThread = new Thread(() -> acceptClients(serverSocketClients, server));
            Thread workerAcceptThread = new Thread(() -> acceptWorkers(serverSocketWorkers, server, id_worker));

            // Start the threads
            clientAcceptThread.start();
            workerAcceptThread.start();

            // Wait for threads to finish
            clientAcceptThread.join();
            workerAcceptThread.join();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void acceptClients(ServerSocket serverSocket, Server server) {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from " + clientSocket.getInetAddress());
                Thread clientThread = new Thread(new ClientHandler(clientSocket, server));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void acceptWorkers(ServerSocket serverSocket, Server server, int id_worker) {
        try {
            while (true) {
                Socket workerSocket = serverSocket.accept();
                System.out.println("Worker connected from " + workerSocket.getInetAddress());
                Thread workerThread = new Thread(new WorkerHandler(id_worker, workerSocket, server));
                workerThread.start();
                id_worker++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
