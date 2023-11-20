import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class server {
    private Map<String, Account> accounts;
    private Map<String, PrintWriter> connectedClients;

    private static Queue<ProgramRequest> pendingPrograms;
    private int max_memory;
    private int memory_used;

    // Add locks
    private final Lock accountsLock = new ReentrantLock();
    private final Lock connectedClientsLock = new ReentrantLock();
    private final Lock pendingProgramsLock = new ReentrantLock();

    public Server() {
        this.accounts = new HashMap<>();
        this.connectedClients = new HashMap<>();
        this.pendingPrograms = new LinkedList<>();
        this.max_memory = 1000;
        this.memory_used = 0;
    }

    // Other methods...

    // Methods for adding and removing from the structures with locks

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
