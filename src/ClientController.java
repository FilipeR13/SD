import java.util.Scanner;

public class ClientController {

    private Account u;
    Scanner sc = new Scanner(System.in);

    public ClientController(Account u) {
        this.u = u;
    }

    public void Login() {
        System.out.print("Nome de Utilizador :: ");
        u.setNomeUtilizador(sc.nextLine());
        System.out.print("Palavra-Passe:: ");
        u.setPassword(sc.nextLine());
    }

    public void Register() {
        System.out.print("Nome de Utilizador :: ");
        u.setNomeUtilizador(sc.nextLine());
        System.out.print("Palavra-Passe:: ");
        u.setPassword(sc.nextLine());
    }
}
