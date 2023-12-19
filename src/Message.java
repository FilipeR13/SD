import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message {
    String type;
    String payload;

    // Constructor

    public Message(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    // Getters and setters

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    // creates the payload of the message with the given array of strings

    public static String createPayload(String[] args) {
        String payload = "";
        for (int i = 0; i < args.length; i++) {
            payload += args[i];
            if (i != args.length - 1) {
                payload += ";";
            }
        }
        return payload;
    }

    // takes the payload of the message and returns an array of strings

    public static String[] parsePayload(String payload) {
        return payload.split(";");
    }

    // serialize and deserialize

    public static void serialize(DataOutputStream out, String type, String payload) throws IOException {
        out.writeUTF(type);
        out.writeUTF(payload);
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        String type = in.readUTF();
        String payload = in.readUTF();
        return new Message(type, payload);
    }
}
