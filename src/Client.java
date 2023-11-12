import sd23.JobFunctionException;

import java.io.*;
import java.net.*;

public class Client {
    private static Account acc = new Account();
    private static ClientController clientC = new ClientController(acc);

    public static void main(String[] args) throws JobFunctionException, IOException {
        //establish connection to the server
        clientC.establishConnection();

        ClientView clientView = new ClientView(clientC);
        clientView.mainMenu();

        // close connection to the server
        clientC.closeConnection();
    }
}
