public class ClientView {
    private ClientController client;

    public ClientView(ClientController client) {
        this.client = client;
    }

    public void run() {
        Menu menuPrincipal = new Menu(new String[]{
                "Login",
                "Register"
        });

        menuPrincipal.setHandler(1,client :: Login);
        menuPrincipal.setHandler(2,client :: Register);
        menuPrincipal.run();
    }
}
