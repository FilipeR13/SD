import sd23.JobFunctionException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


public class Menu {
    public interface Handler {
        public void execute() throws JobFunctionException, IOException;
    }

    public interface PreCondition {
        public boolean condition();
    }

    private List<Handler> handlers;
    private List<PreCondition> preConditions;
    private List<String> options;

    public Menu(String[] options) {
        this.options = Arrays.asList(options);
        this.handlers = new ArrayList<>();
        this.preConditions = new ArrayList<>();
        this.options.forEach(s -> {
            this.preConditions.add(() -> true);
            this.handlers.add(() -> System.out.println("\nATENÇÃO: Opção não implementada!"));
        });
    }

    public void run() throws JobFunctionException, IOException {
        int choice;
        do {
            showMenu();
            choice = readChoice();
            if (choice > 0 && !this.preConditions.get(choice - 1).condition()) {
                System.out.println("Opção indisponível! Tente novamente.");
            } else if (choice > 0) {
                    this.handlers.get(choice - 1).execute();
            }
        } while (choice != 0);
    }

    public void setHandler(int i, Handler h) {
        this.handlers.set(i - 1, h);
    }

    public void setPreCondition(int i, PreCondition b) {
        this.preConditions.set(i - 1, b);
    }

    public void showMenu() {
        System.out.println("-----MENU-----");
        for (int i = 0; i < this.options.size(); i++) {
            System.out.println(i + 1 + " - " + this.options.get(i));
        }
        System.out.println("0 - EXIT");
    }

    public int readChoice() {
        int choice = -1;
        Scanner sc = new Scanner(System.in);
        System.out.print("Choice: ");

        try {
            choice = sc.nextInt();
            if (choice < 0 || choice > this.options.size()) {
                choice = -1;
                System.out.println("INVALID CHOICE!");
            }
        } catch (NoSuchElementException e) {
            // Handle end-of-file (Ctrl+D or Ctrl+Z)
            System.out.println("Client disconnecting!");
            choice = 0;
        }
        return choice;
    }

}
