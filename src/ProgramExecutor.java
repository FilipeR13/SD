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

    public void run() {
        server.setMemory_used(server.getMemory_used() + pr.getMemory());
        PrintWriter clientOut = null;

        try {
            byte[] output = JobFunction.execute(pr.getFile());
            clientOut = server.getConnectedClient(pr.getClientUsername());

            if (clientOut != null) {
                clientOut.println("JOB_DONE");
                clientOut.println(pr.getPedido_id());
                clientOut.println(Arrays.toString(output));
                clientOut.flush();
                System.err.println("success, returned "+output.length+" bytes");
            }
        } catch (JobFunctionException e) {
            System.err.println("job failed: code="+e.getCode()+" message="+e.getMessage());
        } finally {
            server.setMemory_used(server.getMemory_used() - pr.getMemory());
        }
    }
}
