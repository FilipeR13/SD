import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProgramHandler implements Runnable {

    private Server server;
    private ExecutorService threadPool;

    private static final int MAX_THREADS = 10;

    public ProgramHandler(Server server) {
        this.server = server;
        this.threadPool = Executors.newFixedThreadPool(MAX_THREADS);
    }

    public void run() {
        System.out.println("Program Handler running");
        while (true) {
            if (!server.isEmptyPendingPrograms() && server.getMemory_used() + server.getPendingPrograms().peek().getMemory() <= server.getMax_memory()) {
                System.out.println("Estou aqui!");
                ProgramRequest pr = server.getPendingPrograms().poll();
                threadPool.execute(new ProgramExecutor(pr, server));
            }
        }
    }
}