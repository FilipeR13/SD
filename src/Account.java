public class Account {
    private String nomeUtilizador;
    private String password;

    public Account() {
        this.nomeUtilizador = "";
        this.password = "";
    }
    public Account(String nomeUtilizador, String password) {
        this.nomeUtilizador = nomeUtilizador;
        this.password = password;
    }

    public String getNomeUtilizador() {
        return nomeUtilizador;
    }

    public String getPassword() {
        return password;
    }

    public void setNomeUtilizador(String nomeUtilizador) {
        this.nomeUtilizador = nomeUtilizador;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
