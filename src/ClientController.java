import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JobFunctionException e) {
            throw new RuntimeException(e);
        }
    }

    public void register() {
        System.out.print("Nome de Utilizador :: ");
        String username = sc.nextLine();
        System.out.print("Palavra-Passe :: ");
        String password = sc.nextLine();

        // Send registration information to the server without explicit action
        out.println("REGISTER");
        out.println(username);
        out.println(password);

        // Receive a response from the server
        try {
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendProgram() throws IOException {

        System.out.print("Path do ficheiro :: ");
        String file_name = sc.nextLine();

        System.out.print("Memória para o programa (MB):: ");
        String memoria = sc.nextLine();


        File file_execute = new File(file_name);
        FileInputStream read_file = new FileInputStream(file_execute);
        byte[] array = new byte[(int) file_execute.length()];

        // read file into bytes[] and closing the file input stream to avoid memory leakage
        read_file.read(array);
        read_file.close();

        // Send byte array to the server

        out.println("SEND_PROGRAM");
        out.println(memoria);
        out.println(Arrays.toString(array));

        try {
            System.out.println("Waiting for server response");
            String response = in.readLine();
            String var = in.readLine();
            System.out.println("After server response");

            if (response.equals("Not enough memory")) {
                System.out.println("Not enough memory");
                return;
            }
            // aplicar uma expressao a todos elementos de um array
            Object[] result = Arrays.stream(response.substring(1, response.length() - 1).split(", ")).map(Byte::parseByte).toArray();
            byte[] result2 = new byte[result.length];
            for (int i = 0; i < result.length; i++) {
                result2[i] = (byte) result[i];
            }
            System.out.println("Server response: " + result2 + " " + var);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
