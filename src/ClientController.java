import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;

public class ClientController {

    private static class ReceiveResponse implements Runnable{
        private BufferedReader in;

        public ReceiveResponse(BufferedReader in){
            this.in = in;
        }

        public void pendingProgram() {
            try{
                String message_type = in.readLine();
                String value = in.readLine();
                String response = in.readLine();

                if(!message_type.equals("JOB_DONE")){
                    return;
                }
                getResult(response, value);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void getResult(String response, String id){
            Object[] result = Arrays.stream(response.substring(1, response.length() - 1).split(", ")).map(Byte::parseByte).toArray();
            byte[] to_byte = new byte[result.length];
            for (int i = 0; i < result.length; i++) {
                to_byte[i] = (byte) result[i];
            }
            String result_string = Arrays.toString(to_byte);
            System.out.println("Server response: " + id + " " + result_string);
        }

        public void run(){
            try {

                String message_type = in.readLine();
                String value = in.readLine();
                String response = in.readLine();

                if (message_type.equals("NOT_AVAILABLE")) {
                    System.out.println(response);
                    pendingProgram();
                }

                if(message_type.equals("JOB_DONE")){
                    getResult(response, value);
                }

                if(message_type.equals("SERVER_STATUS")){
                    System.out.println("The server has " + value + " MB of memory left and there are currently " + response + " jobs waiting to be executed!");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private Account u;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner sc = new Scanner(System.in);

    private int pedido_id = 1;

    public ClientController(Account u) {
        this.u = u;
    }

    public void establishConnection() {
        try {
            socket = new Socket("localhost", 9090);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() throws IOException {
        // close the connection to the server
        try {
            in.close();
            out.close();
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void login() {
        System.out.print("Nome de Utilizador :: ");
        String username = sc.nextLine();
        System.out.print("Palavra-Passe:: ");
        String password = sc.nextLine();

        // Send login information to the server

        LoginMessage.serialize(out, username, password);
        out.flush();

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
        this.u.setNomeUtilizador(username);
        System.out.print("Palavra-Passe :: ");
        String password = sc.nextLine();
        this.u.setPassword(password);

        // Send registration information to the server without explicit action

        RegisterMessage.serialize(out, username, password);
        out.flush();

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

        SendProgramMessage.serialize(out, u.getNomeUtilizador(), pedido_id, Integer.parseInt(memoria), array);
        out.flush();
        this.pedido_id++;

        Thread t = new Thread(new ReceiveResponse(in));
        t.start();
    }

    public void serverAvailability() {
        out.println("SERVER_AVAILABILITY");
        out.flush();
        Thread t = new Thread(new ReceiveResponse(in));
        t.start();
    }
}

