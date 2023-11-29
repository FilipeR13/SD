import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.*;
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
        DataInputStream in = null;
        DataOutputStream out = null;
        String username = null;

        try {
            // Create input and output streams for communication
            in = new DataInputStream(this.clientSocket.getInputStream());
            out = new DataOutputStream(this.clientSocket.getOutputStream());

            // Keep the connection open for ongoing communication
            while (true) {
                // Read data from the client
                String action = in.readUTF();
                switch (action) {
                    case "LOGIN":
                        this.handleLogin(in, out);
                        break;
                    case "REGISTER":
                        username = this.handleRegister(in, out);
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
                if(username != null) server.removeConnectedClient(username);
                in.close();
                out.close();
                this.clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleLogin(DataInputStream in, DataOutputStream out) throws IOException {
        LoginMessage login = LoginMessage.deserialize(in);

        // Check if the user exists and the password is correct
        if (server.containsAccount(login.getUsername()) && server.getAccount(login.getUsername()).getPassword().equals(login.getPassword())) {
            out.writeUTF("Login successful");
            out.flush();

            // Keep the connection open for ongoing communication
            while (true) {
                String clientMessage = in.readUTF();
                if (clientMessage == null)
                    return;
                switch (clientMessage) {
                    case "SEND_PROGRAM":
                        SendProgramMessage spm = SendProgramMessage.deserialize(in);
                        ProgramRequest pr = new ProgramRequest(spm.getNome_utilizador(), spm.getPedido_id(), spm.getMemoria(), spm.getPrograma().getBytes());
                        server.addPendingProgram(pr);
                        new Thread(() -> {
                            try {
                                server.sendProgram(pr);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                        break;
                    case "SERVER_AVAILABILITY":
                        new Thread(() -> {
                            try {
                                int max_memory_available = 0;
                                for(Worker w : server.getConnectedWorkers()) {
                                    if(w.getMemory_available() > max_memory_available) max_memory_available = w.getMemory_available();
                                }
                                out.writeUTF("SERVER_STATUS");
                                out.writeUTF(String.valueOf(max_memory_available));
                                out.writeUTF(String.valueOf(server.sizePendingPrograms()));
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

    public String handleRegister(DataInputStream in, DataOutputStream out) throws IOException {
        RegisterMessage register = RegisterMessage.deserialize(in);

        // Check if the username already exists
        if (!server.containsAccount(register.getUsername())) {

            // Register the new user
            Account newAccount = new Account(register.getUsername(), register.getPassword());
            server.addAccount(register.getUsername(),newAccount);
            out.writeUTF("Registration successful");
            out.flush();

            // Store the connected client's PrintWriter
            server.addConnectedClient(register.getUsername(), out);
        } else {
            out.writeUTF("Username already exists");
            out.flush();
        }
        return register.getUsername();
    }
}
