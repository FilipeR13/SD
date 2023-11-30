import java.io.*;
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
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
            // Create input and output streams for communication
            in = new DataInputStream(this.workerSocket.getInputStream());
            out = new DataOutputStream(this.workerSocket.getOutputStream());

            // Keep the connection open for ongoing communication
            while (true) {
                // Read data from the client
                String action = in.readUTF();
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
                        out.writeUTF("Invalid action");
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

    public void handleJobDone(String action, DataInputStream in, int worker_id) throws IOException {
        JobDoneMessage jobDoneMessage = JobDoneMessage.deserialize(in);

        server.changeMemoryWorkerPerId(worker_id,jobDoneMessage.getMemory_used());
        DataOutputStream clientOut = server.getConnectedClients().get(jobDoneMessage.getNome_utilizador());

        JobDoneMessage.serialize(clientOut,jobDoneMessage.getNome_utilizador(),jobDoneMessage.getMemory_used(),jobDoneMessage.getPedido_id(),jobDoneMessage.getResult());
    }

    public void handleMemoryInfo(DataInputStream in, DataOutputStream out) throws IOException {
        MemoryInfoMessage memoryInfoMessage = MemoryInfoMessage.deserialize(in);

        server.addConnectedWorker(new Worker(this.worker_id, out, memoryInfoMessage.getMax_memory() - memoryInfoMessage.getMemory_used()));
    }
}




