import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class WorkerServer {

    private int max_memory;
    private int memory_used;

    //constructor

    public WorkerServer() {
        this.max_memory = 1000;
        this.memory_used = 0;
    }

    public WorkerServer(int max_memory, int memory_used) {
        this.max_memory = max_memory;
        this.memory_used = memory_used;
    }

    //getters and setters

    public int getMax_memory() {
        return this.max_memory;
    }

    public int getMemory_used() {
        return this.memory_used;
    }

    public void setMax_memory(int max_memory) {
        this.max_memory = max_memory;
    }

    public void setMemory_used(int memory_used) {
        this.memory_used = memory_used;
    }

    // server socket in which the server listens for connections

    private static final String SERVER_IP = "localhost";  // Replace with your Main Server's IP address
    private static final int SERVER_PORT = 9091;  // Replace with your Main Server's port

    public void sendMemoryInfo(PrintWriter out) {
        out.println("MEMORY_INFO");
        out.println(this.max_memory);
        out.println(this.memory_used);
        out.flush();
    }

    public static void main(String[] args) throws IOException {
        WorkerServer workerServer = new WorkerServer();
        try {
            // Connect to the Main Server
            Socket workerSocket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Connected to Main Server: " + workerSocket.getInetAddress().getHostAddress());

            // Set up input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
            PrintWriter out = new PrintWriter(workerSocket.getOutputStream());

            workerServer.sendMemoryInfo(out);

            while (true) {
                // Read data from the server
                String action = in.readLine();
                if (action == null) {
                    System.out.println("Server disconnected!");
                    return;
                }
                if(action.equals("SEND_PROGRAM")) {
                    String username_client = in.readLine();
                    int id = Integer.parseInt(in.readLine());
                    int memoria = Integer.parseInt(in.readLine());
                    byte[] file = in.readLine().getBytes();
                    ProgramRequest pr = new ProgramRequest(username_client, id, memoria, file);
                    Thread t = new Thread(new ProgramExecutor(pr, workerServer,out));
                    t.start();
                }
                else {
                    out.println("Invalid action");
                    out.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
