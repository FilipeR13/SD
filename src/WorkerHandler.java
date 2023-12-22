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
        SafeDataInputStream in = null;
        SafeDataOutputStream out = null;
        try {
            // Create input and output streams for communication
            in = new SafeDataInputStream(this.workerSocket.getInputStream());
            out = new SafeDataOutputStream(this.workerSocket.getOutputStream());

            // Keep the connection open for ongoing communication
            while (true) {
                // Read data from the client
                Message workerMessage = Message.deserialize(in);
                if (workerMessage.getType() == null) {
                    System.out.println("Worker disconnected!");
                    return;
                }
                switch (workerMessage.getType()) {
                    case "MEMORY_INFO":
                        this.handleMemoryInfo(in,out,workerMessage);
                        break;
                    case "JOB_DONE":
                        this.handleJobCompleted(in, workerMessage, "JOB_DONE");
                        break;
                    case "JOB_FAILED":
                        this.handleJobCompleted(in,workerMessage,"JOB_FAILED");
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

    public void handleJobCompleted(SafeDataInputStream in, Message workerMessage, String message_type) throws IOException {
        String arguments[] = Message.parsePayload(workerMessage.getPayload());
        server.changeMemoryWorkerPerId(worker_id,Integer.parseInt(arguments[1]));
        SafeDataOutputStream clientOut = server.getConnectedClients().get(arguments[0]);
        // decrement the num_jobs of the worker
        server.decrementNumJobsWorker(worker_id);

        Message.serialize(clientOut,message_type ,workerMessage.getPayload());
        clientOut.flush();
    }

    public void handleMemoryInfo(SafeDataInputStream in, SafeDataOutputStream out, Message workerMessage) throws IOException {
        String arguments[] = Message.parsePayload(workerMessage.getPayload());

        server.addConnectedWorker(new Worker(this.worker_id, out, Integer.parseInt(arguments[0]) - Integer.parseInt(arguments[1])));
    }
}




