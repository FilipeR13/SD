import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;

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
        //set the memory used by the worker
        server.setMemory_used(server.getMemory_used() + pr.getMemory());

        try {
            byte[] output = JobFunction.execute(pr.getFile());
            if (out != null) {
                Message.serialize(out,"JOB_DONE", pr.getClientUsername() + ";" + pr.getMemory() + ";" + pr.getPedido_id() + ";" + Arrays.toString(output));
                out.flush();
                System.err.println("success, returned " + output.length + " bytes");
            }
        } catch (JobFunctionException e) {
            System.err.println("job failed: code=" + e.getCode() + " message=" + e.getMessage());
            if (out != null) {
                try {
                    Message.serialize(out,"JOB_FAILED",pr.getClientUsername() + ";" + pr.getMemory() + ";" + pr.getPedido_id() + ";" + e.getCode() + ";" + e.getMessage());
                    out.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            server.setMemory_used(server.getMemory_used() - pr.getMemory());
        }
    }
}
