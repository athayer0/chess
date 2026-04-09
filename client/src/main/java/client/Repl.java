package client;

import java.util.Scanner;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        this.client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to CS240 Chess. Type 'help' to get started. ♕");

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            System.out.print("[" + client.getState().name() + "] >>> ");
            String line = scanner.nextLine();

            try {
                result = client.eval(line);

                if (!result.equals("quit") && !result.isEmpty()) {
                    System.out.println(result);
                }
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Thanks for playing!");
    }
}
