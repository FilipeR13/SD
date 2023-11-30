import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class JobDoneMessage {
    private String nome_utilizador;
    private int memory_used;
    private int pedido_id;
    private String result;

    //constructor

    public JobDoneMessage(String nome_utilizador, int memory_used, int pedido_id, String result) {
        this.nome_utilizador = nome_utilizador;
        this.memory_used = memory_used;
        this.pedido_id = pedido_id;
        this.result = result;
    }

    public JobDoneMessage() {
        this.nome_utilizador = "";
        this.memory_used = 0;
        this.pedido_id = 0;
        this.result = "";
    }

    //getters and setters

    public String getNome_utilizador() {
        return this.nome_utilizador;
    }

    public int getMemory_used() {
        return this.memory_used;
    }

    public int getPedido_id() {
        return this.pedido_id;
    }

    public String getResult() {
        return this.result;
    }

    public void setNome_utilizador(String nome_utilizador) {
        this.nome_utilizador = nome_utilizador;
    }

    public void setMemory_used(int memory_used) {
        this.memory_used = memory_used;
    }

    public void setPedido_id(int pedido_id) {
        this.pedido_id = pedido_id;
    }

    public void setResult(String result) {
        this.result = result;
    }

    //methods

    public static void serialize(DataOutputStream out, String nome_utilizador, int memory_used, int pedido_id, String result) throws IOException {
        out.writeUTF("JOB_DONE");
        out.writeUTF(nome_utilizador);
        out.writeInt(memory_used);
        out.writeInt(pedido_id);
        out.writeUTF(result);
    }

    public static JobDoneMessage deserialize(DataInputStream in) throws IOException {
        String nome_utilizador = in.readUTF();
        int memory_used = in.readInt();
        int pedido_id = in.readInt();
        String result = in.readUTF();
        return new JobDoneMessage(nome_utilizador, memory_used, pedido_id, result);
    }
}
