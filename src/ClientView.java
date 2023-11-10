import sd23.JobFunctionException;

public class ClientView {
    private ClientController client;

    public ClientView(ClientController client) {
        this.client = client;
    }

    public void mainMenu() throws JobFunctionException {
        Menu menuPrincipal = new Menu(new String[]{
                "Login",
                "Register"
        });

        menuPrincipal.setHandler(1,client :: login);
        menuPrincipal.setHandler(2,client :: register);
        menuPrincipal.run();
    }

    public void optionsMenu() throws JobFunctionException {

        Menu optionsMenu = new Menu(new String[]{
                "Send Program",
                "Send Message"
        });

        optionsMenu.setHandler(1,client :: sendProgram);
        optionsMenu.setHandler(2,client :: handleCommunication);
        optionsMenu.run();
    }
}
