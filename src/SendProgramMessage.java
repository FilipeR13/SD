import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class SendProgramMessage {

    private String nome_utilizador;
    private int pedido_id;
    private int memoria;
    private byte[] programa;

    // Constructor and other methods...

    public SendProgramMessage(String nome_utilizador, int pedido_id, int memoria, byte[] programa) {
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

    public byte[] getPrograma() {
        return this.programa;
    }

    public void setNome_utilizador(){
        this.nome_utilizador = nome_utilizador;
    }

    public void setPedido_id(){
        this.pedido_id = pedido_id;
    }

    public void setMemoria(){
        this.memoria = memoria;
    }

    public void setPrograma(){
        this.programa = programa;
    }

    public static void serialize(PrintWriter out, String nome_utilizador, int pedido_id, int memoria, byte[] programa) {
        out.println("SEND_PROGRAM");
        out.println(nome_utilizador);
        out.println(pedido_id);
        out.println(memoria);
        out.println(Arrays.toString(programa));
    }

    public static SendProgramMessage deserialize(BufferedReader in) throws IOException {
        String nome_utilizador = in.readLine();
        int pedido_id = Integer.parseInt(in.readLine());
        int memoria = Integer.parseInt(in.readLine());
        byte[] programa = in.readLine().getBytes();
        return new SendProgramMessage(nome_utilizador, pedido_id, memoria, programa);
    }
}
