import java.io.*;
import java.net.*;

public class Client {
    private static Account acc = new Account();
    private static ClientController clientC = new ClientController(acc);

    public static void main(String[] args) {
        String serverAddress = "localhost"; // IP address of the server
        int serverPort = 9090; // Port number the server is listening on
        ClientView clientView = new ClientView(clientC);
        clientView.run();

        try {
            // Create a socket to connect to the server
            Socket socket = new Socket(serverAddress, serverPort);

            // Create input and output streams for communication
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send a message to the server
            out.println("Hello, server!");

            // Receive a response from the server
            String response = in.readLine();
            System.out.println("Server response: " + response);

            // Close the connection
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
