import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientController {
    private Account u;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner sc = new Scanner(System.in);

    public ClientController(Account u) {
        this.u = u;
    }

    public void establishConnection() {
        try {
            socket = new Socket("localhost", 9090);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        // close the connection to the server
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login() {
        // Establish connection to the server
        establishConnection();

        System.out.print("Nome de Utilizador :: ");
        String username = sc.nextLine();
        System.out.print("Palavra-Passe:: ");
        String password = sc.nextLine();

        // Send login information to the server
        out.println("LOGIN");
        out.println(username);
        out.println(password);

        // Receive a response from the server
        try {
            String response = in.readLine();
            System.out.println("Server response: " + response);

            // Check if login was successful
            if (response.equals("Login successful")) {
                ClientView view = new ClientView(this);
                view.optionsMenu();
               // handleCommunication(username, password);
            }
            else{
                closeConnection();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JobFunctionException e) {
            throw new RuntimeException(e);
        }
    }

    public void register() {
        // Establish connection to the server
        establishConnection();

        System.out.print("Nome de Utilizador :: ");
        String username = sc.nextLine();
        System.out.print("Palavra-Passe:: ");
        String password = sc.nextLine();

        // Send registration information to the server without explicit action
        out.println("REGISTER");
        out.println(username);
        out.println(password);

        // Receive a response from the server
        try {
            String response = in.readLine();
            System.out.println("Server response: " + response);

            closeConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendProgram() {
        System.out.println("OI");
    }

    public void handleCommunication() {
        try {

            Thread communicationThread = new Thread(() -> {
                try {
                    while (true) {

                        // Read user input if available
                        if (sc.hasNextLine()) {
                            String userInput = sc.nextLine();

                            // Send user input as a message to the server
                            out.println("SEND_MESSAGE");
                            out.println(userInput);
                        }

                        // Read messages from the server
                        String serverMessage = in.readLine();
                        if (serverMessage == null) {
                            // Server closed the connection
                            System.out.println("Server disconnected");
                            closeConnection();
                            break;
                        }
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            communicationThread.start();

            communicationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
