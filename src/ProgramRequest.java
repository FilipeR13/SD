public class ProgramRequest {
    private String clientUsername;
    private int memory;
    private byte[] file;
    private int pedido_id;

    public ProgramRequest(){
        this.clientUsername = "";
        this.pedido_id = 0;
        this.memory = 0;
        this.file = new byte[0];
    }

    public ProgramRequest(String []arguments) {
        this.clientUsername = arguments[0];
        this.pedido_id = Integer.parseInt(arguments[1]);
        this.memory = Integer.parseInt(arguments[2]);
        this.file = arguments[3].getBytes();
    }

    public ProgramRequest(ProgramRequest pr) {
        this.clientUsername = pr.getClientUsername();
        this.pedido_id = pr.getPedido_id();
        this.memory = pr.getMemory();
        this.file = pr.getFile();
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public int getPedido_id() {
        return pedido_id;
    }

    public int getMemory() {
        return memory;
    }

    public byte[] getFile() {
        return file;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    public void setPedido_id(int pedido_id) {
        this.pedido_id = pedido_id;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }
}
