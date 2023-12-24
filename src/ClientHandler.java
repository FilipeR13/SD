import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Server server;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public void closeconnectionClient(SafeDataInputStream in, SafeDataOutputStream out, String username) throws IOException {
        // Close the streams and clientSocket
        if(username != null) server.removeConnectedClient(username);
        in.close();
        out.close();
        this.clientSocket.close();
    }

    public void run(){
        SafeDataInputStream in = null;
        SafeDataOutputStream out = null;
        String username = null;

        try {
            // Create input and output streams for communication
            in = new SafeDataInputStream(this.clientSocket.getInputStream());
            out = new SafeDataOutputStream(this.clientSocket.getOutputStream());

            // Keep the connection open for ongoing communication
            while (true) {
                // Read data from the client
                Message clientMessage = Message.deserialize(in);

                if (clientMessage == null) {
                    System.out.println("Client disconnected!");
                    return;
                }
                switch (clientMessage.getType()) {
                    case "LOGIN":
                        this.handleLogin(in, out, clientMessage);
                        break;
                    case "REGISTER":
                        username = this.handleRegister(in, out, clientMessage);
                        break;
                    default:
                        out.writeUTF("Invalid action");
                        out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the streams and clientSocket
                closeconnectionClient(in,out,username);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleLogin(SafeDataInputStream in, SafeDataOutputStream out, Message clientMessage) throws IOException {
        String arguments[] = Message.parsePayload(clientMessage.getPayload());

        // Check if the user exists and the password is correct
        if (server.containsAccount(arguments[0]) && server.getAccount(arguments[0]).getPassword().equals(arguments[1])) {
            out.writeUTF("Login successful");
            out.flush();

            // Keep the connection open for ongoing communication
            while (true) {
                // Read data from the client
                Message message = Message.deserialize(in);

                if (message == null) {
                    System.out.println("Client disconnected!");
                    return;
                }

                switch (message.getType()) {
                    case "SEND_PROGRAM":
                        String values[] = Message.parsePayload(message.getPayload());
                        ProgramRequest pr = new ProgramRequest(values);
                        server.addPendingProgram(pr);
                        break;
                    case "SERVER_AVAILABILITY":
                        new Thread(() -> {
                            try {
                                int max_memory_available = 0;
                                for(Worker w : server.getConnectedWorkers().values()) {
                                    if(w.getMemory_available() > max_memory_available) max_memory_available = w.getMemory_available();
                                }
                                Message.serialize(out, "SERVER_STATUS",max_memory_available + ";" + server.sizePendingPrograms());
                                out.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                        break;
                    default:
                        out.writeUTF("Invalid action");
                        out.flush();
                }
            }
        } else {
            out.writeUTF("Invalid username or password");
            out.flush();
        }
    }

    public String handleRegister(SafeDataInputStream in, SafeDataOutputStream out, Message clientMessage) throws IOException {
        String arguments[] = Message.parsePayload(clientMessage.getPayload());

        // Check if the username already exists
        if (!server.containsAccount(arguments[0])) {

            // Register the new user
            Account newAccount = new Account(arguments[0], arguments[1]);
            server.addAccount(arguments[0],newAccount);
            out.writeUTF("Registration successful");
            out.flush();

            // Store the connected client's PrintWriter
            server.addConnectedClient(arguments[0], out);
        } else {
            out.writeUTF("Username already exists");
            out.flush();
        }
        return arguments[0];
    }
}
