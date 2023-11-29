import sd23.JobFunctionException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class ClientController {

    private static class ReceiveResponse implements Runnable{
        private Account u;
        private DataInputStream in;

        public ReceiveResponse(Account u, DataInputStream in){
            this.u = u;
            this.in = in;
        }

        public void sendToFile(String username, String result, String id){
            try {
                //create file if it doesn't exist

                File file = new File("/home/jojocoelho/Desktop/Projetos/SD/src/resultados",username + ".txt");
                FileWriter writer = new FileWriter(file, true);
                writer.write(id + ": " + result + "\n");
                writer.close();
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

            sendToFile(u.getNomeUtilizador(),result_string, id);
        }

        public void run(){
            try {

                String message_type = in.readUTF();
                String value = in.readUTF();
                String response = in.readUTF();

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
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedReader stdin;

    private int pedido_id = 1;

    public ClientController(Account u) {
        this.u = u;
    }

    public void establishConnection() {
        try {
            socket = new Socket("localhost", 9090);
            stdin = new BufferedReader(new InputStreamReader(System.in));
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
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

        //erase client results file from the folder "resultados"

        File file = new File("/home/jojocoelho/Desktop/Projetos/SD/src/resultados",u.getNomeUtilizador() + ".txt");
        file.delete();
    }

    public void login() throws IOException {
        System.out.print("Nome de Utilizador :: ");
        String username = stdin.readLine();
        System.out.print("Palavra-Passe:: ");
        String password = stdin.readLine();

        // Send login information to the server

        LoginMessage.serialize(out, username, password);
        out.flush();

        // Receive a response from the server
        try {
            String response = in.readUTF();
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

    public void register() throws IOException {
        System.out.print("Nome de Utilizador :: ");
        String username = stdin.readLine();
        this.u.setNomeUtilizador(username);
        System.out.print("Palavra-Passe :: ");
        String password = stdin.readLine();
        this.u.setPassword(password);

        // Send registration information to the server without explicit action

        RegisterMessage.serialize(out, username, password);
        out.flush();

        // Receive a response from the server
        try {
            String response = in.readUTF();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendProgram() throws IOException {

        System.out.print("Path do ficheiro :: ");
        String file_name = stdin.readLine();

        System.out.print("Memória para o programa (MB):: ");
        String memoria = stdin.readLine();


        File file_execute = new File(file_name);
        FileInputStream read_file = new FileInputStream(file_execute);
        byte[] array = new byte[(int) file_execute.length()];

        // read file into bytes[] and closing the file input stream to avoid memory leakage
        read_file.read(array);
        read_file.close();

        // Send byte array to the server

        SendProgramMessage.serialize(out, u.getNomeUtilizador(), pedido_id, Integer.parseInt(memoria), Arrays.toString(array));
        out.flush();
        this.pedido_id++;

        Thread t = new Thread(new ReceiveResponse(this.u,in));
        t.start();
    }

    public void serverAvailability() throws IOException {
        out.writeUTF("SERVER_AVAILABILITY");
        out.flush();
        Thread t = new Thread(new ReceiveResponse(this.u,in));
        t.start();
    }
}

