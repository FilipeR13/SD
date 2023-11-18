import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {

    public static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private Server server;

        public ClientHandler(Socket clientSocket, Server server) {
            this.clientSocket = clientSocket;
            this.server = server;
        }

        public void run(){
            BufferedReader in = null;
            PrintWriter out = null;
            String username = null;

            try {
                // Create input and output streams for communication
                in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                out = new PrintWriter(this.clientSocket.getOutputStream(), true);

                // Keep the connection open for ongoing communication
                while (true) {
                    // Read data from the client
                    String action = in.readLine();
                    if (action == null) {
                        System.out.println("Client disconnected!");
                        return;
                    }

                    switch (action) {
                        case "LOGIN":
                            this.handleLogin(in, out);
                            break;
                        case "REGISTER":
                            username = this.handleRegister(in, out);
                            break;
                        default:
                            out.println("Invalid action");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Close the streams and clientSocket
                    if(username != null) server.connectedClients.remove(username);
                    in.close();
                    out.close();
                    this.clientSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void handleLogin(BufferedReader in, PrintWriter out) throws IOException {
            String username = in.readLine();
            String password = in.readLine();

            // Check if the user exists and the password is correct
            if (server.accounts.containsKey(username) && server.accounts.get(username).getPassword().equals(password)) {
                out.println("Login successful");

                // Keep the connection open for ongoing communication
                while (true) {
                    String clientMessage = in.readLine();
                    if (clientMessage == null)
                        return;
                    switch (clientMessage) {
                        case "SEND_PROGRAM":
                            handleSendProgram(in, out);
                            break;
                        case "SERVER_AVAILABILITY":
                            out.println("SERVER_STATUS");
                            out.println(server.max_memory - server.memory_used);
                            out.println(server.pendingPrograms.size());
                            break;
                        default:
                            out.println("Invalid action");
                    }
                }
            } else {
                out.println("Invalid username or password");
            }
        }

        public String handleRegister(BufferedReader in, PrintWriter out) throws IOException {
            String username = in.readLine();
            String password = in.readLine();

            // Check if the username already exists
            if (!server.accounts.containsKey(username)) {

                // Register the new user
                Account newAccount = new Account(username, password);
                server.accounts.put(username, newAccount);
                out.println("Registration successful");

                // Store the connected client's PrintWriter
                server.connectedClients.put(username, out);
            } else {
                out.println("Username already exists");
            }

            return username;
        }

        public void handleSendProgram(BufferedReader in, PrintWriter out) throws IOException{
            System.out.println("Received request to execute job");

            String username = in.readLine();
            int id = Integer.parseInt(in.readLine());
            int memoria = Integer.parseInt(in.readLine());
            byte[] file = in.readLine().getBytes();

            if (memoria > server.max_memory - server.memory_used){
                out.println("NOT_AVAILABLE");
                out.println(id);
                out.println("Not enough memory available, the job will be executed later.");
                server.pendingPrograms.offer(new ProgramRequest(username, id, memoria, file));
                return;
            }
            server.memory_used += memoria;

            try{
                byte[] output = JobFunction.execute(file);
                System.out.println(Arrays.toString(output));

                out.println("JOB_DONE");
                out.println(id);
                out.println(Arrays.toString(output));
            }catch (JobFunctionException e){
                out.println("job failed: code="+e.getCode()+" message="+e.getMessage());
            }
            server.memory_used -= memoria;

            executePendingPrograms();
        }

        public void executePendingPrograms() {
            while (!server.pendingPrograms.isEmpty() && enoughMemoryAvailable(server.pendingPrograms.peek())) {
                ProgramRequest pendingProgram = server.pendingPrograms.poll();
                server.memory_used += pendingProgram.getMemory();
                PrintWriter clientOut = null;

                try {
                    byte[] output = JobFunction.execute(pendingProgram.getFile());
                    clientOut = server.connectedClients.get(pendingProgram.getClientUsername());

                    if (clientOut != null) {
                        clientOut.println("JOB_DONE");
                        clientOut.println(pendingProgram.getPedido_id());
                        clientOut.println(Arrays.toString(output));
                    }
                } catch (JobFunctionException e) {
                    clientOut.println("job failed: code="+e.getCode()+" message="+e.getMessage()); // Handle exception as needed
                }

                server.memory_used -= pendingProgram.getMemory();
            }
        }

        public boolean enoughMemoryAvailable(ProgramRequest program) {
            return program.getMemory() <= (server.max_memory - server.memory_used);
        }
    }

    private Map<String, Account> accounts;
    private Map<String, PrintWriter> connectedClients;

    private Queue<ProgramRequest> pendingPrograms;
    private int max_memory;
    private int memory_used;

    public Server(){
        this.accounts = new HashMap<>();
        this.connectedClients = new HashMap<>();
        this.pendingPrograms = new LinkedList<>();
        this.max_memory = 1000;
        this.memory_used = 0;
    }

    public static void main(String[] args) throws InterruptedException{
        int port = 9090; // Choose a port number
        Server server = new Server();

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);

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
