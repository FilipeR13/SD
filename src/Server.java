import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private Map<String, Account> accounts;
    private Map<String, PrintWriter> connectedClients;
    private static PriorityQueue<ProgramRequest> pendingPrograms;

    private int max_memory;
    private int memory_used;

    private final Lock accountsLock = new ReentrantLock();
    private final Lock connectedClientsLock = new ReentrantLock();
    private final Lock pendingProgramsLock = new ReentrantLock();


    public Server(){
        this.accounts = new HashMap<>();
        this.connectedClients = new HashMap<>();
        this.pendingPrograms = new PriorityQueue<>(new ProgramRequestComparator());
        this.max_memory = 1000;
        this.memory_used = 0;
    }

    // getters and setters

    public Map<String, Account> getAccounts() {
        return accounts;
    }

    public Map<String, PrintWriter> getConnectedClients() {
        return connectedClients;
    }

    public PriorityQueue<ProgramRequest> getPendingPrograms() {
        return pendingPrograms;
    }

    public int getMax_memory() {
        return max_memory;
    }

    public int getMemory_used() {
        return memory_used;
    }

    public void setAccounts(Map<String, Account> accounts) {
        this.accounts = accounts;
    }

    public void setConnectedClients(Map<String, PrintWriter> connectedClients) {
        this.connectedClients = connectedClients;
    }

   public void setPendingPrograms(PriorityQueue<ProgramRequest> pendingPrograms) {
            this.pendingPrograms = pendingPrograms;
        }

    public void setMax_memory(int max_memory) {
        this.max_memory = max_memory;
    }

    public void setMemory_used(int memory_used) {
        this.memory_used = memory_used;
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

    public void addConnectedClient(String username, PrintWriter out) {
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

    // get a specific element from the structures

    public Account getAccount(String username) {
        accountsLock.lock();
        try {
            return this.accounts.get(username);
        } finally {
            accountsLock.unlock();
        }
    }

    public PrintWriter getConnectedClient(String username) {
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

    // main method

    public static void main(String[] args) throws InterruptedException{
        int port = 9090; // Choose a port number
        Server server = new Server();
        Thread t = new Thread(new ProgramHandler(server));
        t.start();

        try {

            // Create a server socket

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);

            // Keep accepting client connections in a while true loop
            while (true) {

                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from " + clientSocket.getInetAddress());


                // Handle client connection in a separate thread
                Thread clientThread = new Thread(new ClientHandler(clientSocket,server));
                clientThread.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
