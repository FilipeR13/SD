import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MemoryInfoMessage {
    private int max_memory;
    private int memory_used;

    //constructor

    public MemoryInfoMessage(int max_memory, int memory_used) {
        this.max_memory = max_memory;
        this.memory_used = memory_used;
    }

    public MemoryInfoMessage() {
        this.max_memory = 0;
        this.memory_used = 0;
    }

    //getters and setters

    public int getMax_memory() {
        return this.max_memory;
    }

    public int getMemory_used() {
        return this.memory_used;
    }

    public void setMax_memory(int max_memory) {
        this.max_memory = max_memory;
    }

    public void setMemory_used(int memory_used) {
        this.memory_used = memory_used;
    }

    //methods

    public static void serialize(DataOutputStream out, int max_memory, int memory_used) throws IOException {
        out.writeUTF("MEMORY_INFO");
        out.writeInt(max_memory);
        out.writeInt(memory_used);
    }

    public static MemoryInfoMessage deserialize(DataInputStream in) throws IOException {
        int max_memory = in.readInt();
        int memory_used = in.readInt();
        return new MemoryInfoMessage(max_memory, memory_used);
    }
}
