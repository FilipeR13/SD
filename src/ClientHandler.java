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
            out = new PrintWriter(this.clientSocket.getOutputStream());

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
                        out.flush();
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
            out.flush();

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
                        ProgramRequest pr = new ProgramRequest(username_client, id, memoria, file);
                        server.addPendingProgram(pr);
                        new Thread(() -> server.sendProgram(pr)).start();
                        break;
                    case "SERVER_AVAILABILITY":
                        new Thread(() -> {
                            try {
                                int max_memory_available = 0;
                                for(Worker w : server.getConnectedWorkers()) {
                                    if(w.getMemory_available() > max_memory_available) max_memory_available = w.getMemory_available();
                                }
                                out.println("SERVER_STATUS");
                                out.println(max_memory_available);
                                out.println(server.sizePendingPrograms());
                                out.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                        break;
                    default:
                        out.println("Invalid action");
                        out.flush();
                }
            }
        } else {
            out.println("Invalid username or password");
            out.flush();
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
            out.flush();

            // Store the connected client's PrintWriter
            server.addConnectedClient(username, out);
        } else {
            out.println("Username already exists");
            out.flush();
        }

        return username;
    }
}
