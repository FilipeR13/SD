import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private Map<String, Account> accounts = new HashMap<>();
    private Map<String, PrintWriter> connectedClients = new HashMap<>();

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
        try {
            // Create input and output streams for communication
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read data from the client
            String action = in.readLine();

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

            // Close the connections
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
                if (clientMessage.equals("quit")) {
                    // Client disconnected or requested to disconnect
                    connectedClients.remove(username);
                    break;
                } else {
                    switch (clientMessage) {
                        case "SEND_MESSAGE":
                            handleSendMessage(in, out);
                            break;
                        case "SEND_PROGRAM":
                            handleSendProgram(in, out);
                            break;
                        default:
                            out.println("Invalid action");
                    }
                    // Process the client's message
                    System.out.println("Received from " + username + ": " + clientMessage);

                    // Confirms that the message was received
                    out.println("Server: Message received");
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

    private void handleSendMessage(BufferedReader in, PrintWriter out) throws IOException {
        String senderUsername = in.readLine();
        String receiverUsername = in.readLine();
        String message = in.readLine();

        if (connectedClients.containsKey(receiverUsername)) {
            PrintWriter receiverOut = connectedClients.get(receiverUsername);
            receiverOut.println("Message from " + senderUsername + ": " + message);
        } else {
            out.println("User not online or does not exist");
        }
    }

    private void handleSendProgram(BufferedReader in, PrintWriter out) throws IOException{
        try{
            byte[] file = in.readLine().getBytes();
            byte[] output = JobFunction.execute(file);

            out.println("success, returned "+output.length+" bytes");
        }catch (JobFunctionException e){
            out.println("job failed: code="+e.getCode()+" message="+e.getMessage());
        }
    }
}
