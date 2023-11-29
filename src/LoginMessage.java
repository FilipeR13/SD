import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LoginMessage {

    private String username;
    private String password;

    // Constructor and other methods...

    public LoginMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public static void serialize(DataOutputStream out, String username, String password) throws IOException {
        out.writeUTF("LOGIN");
        out.writeUTF(username);
        out.writeUTF(password);
    }

    public static LoginMessage deserialize(DataInputStream in) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        return new LoginMessage(username, password);
    }

}
