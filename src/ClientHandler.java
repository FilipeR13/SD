import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler implements Runnable {
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
                if(username != null) server.removeConnectedClient(username);
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
        if (server.containsAccount(username) && server.getAccount(username).getPassword().equals(password)) {
            out.println("Login successful");

            // Keep the connection open for ongoing communication
            while (true) {
                String clientMessage = in.readLine();
                if (clientMessage == null)
                    return;
                switch (clientMessage) {
                    case "SEND_PROGRAM":
                        String username_client = in.readLine();
                        int id = Integer.parseInt(in.readLine());
                        int memoria = Integer.parseInt(in.readLine());
                        byte[] file = in.readLine().getBytes();
                        server.addPendingProgram(new ProgramRequest(username_client, id, memoria, file));
                        server.printPendingPrograms();
                        break;
                    case "SERVER_AVAILABILITY":
                        new Thread(() -> {
                            try {
                                out.println("SERVER_STATUS");
                                out.println(server.getMax_memory() - server.getMemory_used());
                                out.println(server.sizePendingPrograms());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
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
        if (!server.containsAccount(username)) {

            // Register the new user
            Account newAccount = new Account(username, password);
            server.addAccount(username,newAccount);
            out.println("Registration successful");

            // Store the connected client's PrintWriter
            server.addConnectedClient(username, out);
        } else {
            out.println("Username already exists");
        }

        return username;
    }
}
