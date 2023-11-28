import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WorkerHandler implements Runnable {

    private Socket workerSocket;
    private Server server;
    private int worker_id;

    //constructor

    public WorkerHandler(int worker_id, Socket workerSocket, Server server) {
        this.worker_id = worker_id;
        this.workerSocket = workerSocket;
        this.server = server;
    }

    public WorkerHandler() {
        this.worker_id = 0;
        this.workerSocket = null;
        this.server = new Server();
    }

    //getters and setters

    public int getWorker_id() {
        return this.worker_id;
    }

    public Socket getWorkerSocket() {
        return this.workerSocket;
    }

    public Server getServer() {
        return this.server;
    }

    public void setWorker_id(int worker_id) {
        this.worker_id = worker_id;
    }

    public void setWorkerSocket(Socket workerSocket) {
        this.workerSocket = workerSocket;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            // Create input and output streams for communication
            in = new BufferedReader(new InputStreamReader(this.workerSocket.getInputStream()));
            out = new PrintWriter(this.workerSocket.getOutputStream());

            // Keep the connection open for ongoing communication
            while (true) {
                // Read data from the client
                String action = in.readLine();
                if (action == null) {
                    System.out.println("Worker disconnected!");
                    return;
                }

                switch (action) {
                    case "MEMORY_INFO":
                        this.handleMemoryInfo(in,out);
                        break;
                    case "JOB_DONE":
                        this.handleJobDone(action,in,this.worker_id);
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
                server.removeConnectedWorker(worker_id);
                in.close();
                out.close();
                this.workerSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleJobDone(String action, BufferedReader in, int worker_id) throws IOException {
        String username = in.readLine();
        int memory = Integer.parseInt(in.readLine());
        int pedido_id = Integer.parseInt(in.readLine());
        String result_string = in.readLine();

        server.changeMemoryWorkerPerId(worker_id,memory);

        // get from the connected clients map the Printwriter of the client that has the username given as key

        PrintWriter clientOut = server.getConnectedClients().get(username);

        clientOut.println(action);
        clientOut.println(pedido_id);
        clientOut.println(result_string);
        clientOut.flush();
    }

    public void handleMemoryInfo(BufferedReader in, PrintWriter out) throws IOException {
        int max_memory = Integer.parseInt(in.readLine());
        int memory_used = Integer.parseInt(in.readLine());

        server.addConnectedWorker(new Worker(this.worker_id, out, max_memory - memory_used));
    }
}




