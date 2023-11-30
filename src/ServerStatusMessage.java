import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerStatusMessage {
    private int memory_available;
    private int number_programs;

    //constructor

    public ServerStatusMessage(int memory_available, int number_programs) {
        this.memory_available = memory_available;
        this.number_programs = number_programs;
    }

    public ServerStatusMessage() {
        this.memory_available = 0;
        this.number_programs = 0;
    }

    //getters and setters

    public int getMemory_available() {
        return this.memory_available;
    }

    public int getNumber_programs() {
        return this.number_programs;
    }

    public void setMemory_available(int memory_available) {
        this.memory_available = memory_available;
    }

    public void setNumber_programs(int number_programs) {
        this.number_programs = number_programs;
    }

    //methods

    public static void serialize(DataOutputStream out, int memory_available, int number_programs) throws IOException {
        out.writeUTF("SERVER_STATUS");
        out.writeInt(memory_available);
        out.writeInt(number_programs);
    }

    public static ServerStatusMessage deserialize(DataInputStream in) throws IOException {
        int memory_available = in.readInt();
        int number_programs = in.readInt();
        return new ServerStatusMessage(memory_available, number_programs);
    }
}
