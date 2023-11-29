import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class SendProgramMessage {

    private String nome_utilizador;
    private int pedido_id;
    private int memoria;
    private String programa;

    // Constructor and other methods...

    public SendProgramMessage(String nome_utilizador, int pedido_id, int memoria, String programa) {
        this.nome_utilizador = nome_utilizador;
        this.pedido_id = pedido_id;
        this.memoria = memoria;
        this.programa = programa;
    }

    public String getNome_utilizador() {
        return this.nome_utilizador;
    }

    public int getPedido_id() {
        return this.pedido_id;
    }

    public int getMemoria() {
        return this.memoria;
    }

    public String getPrograma() {
        return this.programa;
    }

    public void setNome_utilizador(String nome_utilizador){
        this.nome_utilizador = nome_utilizador;
    }

    public void setPedido_id(int pedido_id){
        this.pedido_id = pedido_id;
    }

    public void setMemoria(int memoria){
        this.memoria = memoria;
    }

    public void setPrograma(String programa){
        this.programa = programa;
    }

    public static void serialize(DataOutputStream out, String nome_utilizador, int pedido_id, int memoria, String programa) throws IOException {
        out.writeUTF("SEND_PROGRAM");
        out.writeUTF(nome_utilizador);
        out.writeInt(pedido_id);
        out.writeInt(memoria);
        out.writeUTF(programa);
    }

    public static SendProgramMessage deserialize(DataInputStream in) throws IOException {
        String nome_utilizador = in.readUTF();
        int pedido_id = in.readInt();
        int memoria = in.readInt();
        String programa = in.readUTF();
        return new SendProgramMessage(nome_utilizador, pedido_id, memoria, programa);
    }
}
