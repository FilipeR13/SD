import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class ProgramExecutor implements Runnable {
    private ProgramRequest pr;
    private WorkerServer server;
    private DataOutputStream out;

    public ProgramExecutor(ProgramRequest pr, WorkerServer server, DataOutputStream out) {
        this.pr = pr;
        this.server = server;
        this.out = out;
    }

    public void run() {
        PrintWriter serverOut = null;

        //set the memory used by the worker

        server.setMemory_used(server.getMemory_used() + pr.getMemory());

        try {
            byte[] output = JobFunction.execute(pr.getFile());
            if (out != null) {
                out.writeUTF("JOB_DONE");
                out.writeUTF(pr.getClientUsername());
                out.writeInt(server.getMemory_used());
                out.writeInt(pr.getPedido_id());
                out.writeUTF(Arrays.toString(output));
                out.flush();
                System.err.println("success, returned " + output.length + " bytes");
            }
        } catch (JobFunctionException e) {
            System.err.println("job failed: code=" + e.getCode() + " message=" + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            server.setMemory_used(server.getMemory_used() - pr.getMemory());
        }
    }
}
