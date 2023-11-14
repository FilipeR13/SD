import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private Map<String, Account> accounts = new HashMap<>();
    private Map<String, PrintWriter> connectedClients = new HashMap<>();
    private int max_memory = 1000;
    private int memory_used = 0;

    public static void main(String[] args) {
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
                new Thread(() -> server.handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            // Create input and output streams for communication
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Keep the connection open for ongoing communication
            while (true) {
                // Read data from the client
                String action = in.readLine();
                if (action == null) {
                    System.out.println("Client disconnected");
                    return;
                 }

                switch (action) {
                    case "LOGIN":
                        handleLogin(in, out);
                        break;
                    case "REGISTER":
                        handleRegister(in, out);
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
                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLogin(BufferedReader in, PrintWriter out) throws IOException {
        String username = in.readLine();
        String password = in.readLine();

        // Check if the user exists and the password is correct
        if (accounts.containsKey(username) && accounts.get(username).getPassword().equals(password)) {
            out.println("Login successful");

            // Keep the connection open for ongoing communication
            while (true) {
                String clientMessage = in.readLine();
                if (clientMessage == null)
                    return;
                if (clientMessage.equals("SEND_PROGRAM")) {
                    handleSendProgram(in, out);
                    out.println("Message received");
                    System.out.println("Message received");
                } else {
                    out.println("Invalid action");
                }
            }
        } else {
            out.println("Invalid username or password");
        }
    }

    private void handleRegister(BufferedReader in, PrintWriter out) throws IOException {
        String username = in.readLine();
        String password = in.readLine();

        // Check if the username already exists
        if (!accounts.containsKey(username)) {

            // Register the new user
            Account newAccount = new Account(username, password);
            accounts.put(username, newAccount);
            out.println("Registration successful");

            // Store the connected client's PrintWriter
            connectedClients.put(username, out);
        } else {
            out.println("Username already exists");
        }
    }

    private void handleSendProgram(BufferedReader in, PrintWriter out) throws IOException{
        System.out.println("Received request to execute job");

        int memoria = Integer.parseInt(in.readLine());

        System.out.println("Memoria: " + memoria);
        if (memoria > max_memory - memory_used){
            out.println("Not enough memory");
            return;
        }
        memory_used += memoria;

        System.out.println("Memory used: " + memory_used);

        try{
            byte[] file = in.readLine().getBytes();
            byte[] output = JobFunction.execute(file);
            System.out.println(Arrays.toString(output));
            out.println(Arrays.toString(output));
        }catch (JobFunctionException e){
            out.println("job failed: code="+e.getCode()+" message="+e.getMessage());
        }
        memory_used -= memoria;

        System.out.println("Memory used: " + memory_used);

    }
}
