import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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

    public void setUsername(){
        this.username = username;
    }

    public void setPassword(){
        this.password = password;
    }

    public static void serialize(PrintWriter out, String username, String password) {
        out.println("LOGIN");
        out.println(username);
        out.println(password);
    }

    public static LoginMessage deserialize(BufferedReader in) throws IOException {
        String username = in.readLine();
        String password = in.readLine();
        return new LoginMessage(username, password);
    }

}
