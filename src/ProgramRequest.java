public class ProgramRequest {
    private String clientUsername;
    private int memory;
    private byte[] file;

    public ProgramRequest(){
        this.clientUsername = "";
        this.memory = 0;
        this.file = new byte[0];
    }

    public ProgramRequest(String clientUsername, int memory, byte[] file) {
        this.clientUsername = clientUsername;
        this.memory = memory;
        this.file = file;
    }

    public ProgramRequest(ProgramRequest pr) {
        this.clientUsername = pr.getClientUsername();
        this.memory = pr.getMemory();
        this.file = pr.getFile();
    }

    public String getClientUsername() {
        return clientUsername;
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

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }


}
