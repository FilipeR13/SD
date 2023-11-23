import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.PrintWriter;
import java.util.Arrays;

public class ProgramExecutor implements Runnable {
    private ProgramRequest pr;
    private Server server;

    public ProgramExecutor(ProgramRequest pr, Server server) {
        this.pr = pr;
        this.server = server;
    }


    // erro quando envio varios programas com 1 de memoria no mesmo cliente

    public void run() {
        System.out.println("Executing program " + pr.getPedido_id());
        server.setMemory_used(server.getMemory_used() + pr.getMemory());
        PrintWriter clientOut = null;

        try {
            byte[] output = JobFunction.execute(pr.getFile());
            clientOut = server.getConnectedClient(pr.getClientUsername());

            if (clientOut != null) {
                clientOut.println("JOB_DONE");
                clientOut.println(pr.getPedido_id());
                clientOut.println(Arrays.toString(output));
            }
        } catch (JobFunctionException e) {
            e.printStackTrace();  // Handle exception as needed
        } finally {
            server.setMemory_used(server.getMemory_used() - pr.getMemory());
        }
    }
}
