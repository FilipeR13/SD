import java.io.PrintWriter;

public class Worker {
    private int worker_id;
    private PrintWriter out;
    private int memory_available;

    //constructor

    public Worker(int worker_id, PrintWriter out, int memory_available) {
        this.out = out;
        this.worker_id = worker_id;
        this.memory_available = memory_available;
    }

    public Worker() {
        this.out = null;
        this.worker_id = 0;
        this.memory_available = 0;
    }

    //getters and setters

    public PrintWriter getOut() {
        return this.out;
    }

    public int getWorker_id() {
        return this.worker_id;
    }

    public int getMemory_available() {
        return this.memory_available;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public void setWorker_id(int worker_id) {
        this.worker_id = worker_id;
    }

    public void setMemory_available(int memory_available) {
        this.memory_available = memory_available;
    }
}
