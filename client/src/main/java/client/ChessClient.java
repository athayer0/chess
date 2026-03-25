package client;

import model.AuthData;
import model.UserData;
import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGED_OUT;
    private AuthData authData = null;

    public enum State {
        LOGGED_OUT,
        LOGGED_IN
    }

    public State getState() {
        return state;
    }

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.server = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (state == State.LOGGED_OUT) {
                return evalPreLogin(cmd, params);
            } else {
                return evalPostLogin(cmd, params);
            }
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String evalPreLogin(String cmd, String[] params) throws ResponseException {
        return switch (cmd) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "quit" -> "quit";
            default -> helpPreLogin();
        };
    }

    private String evalPostLogin(String cmd, String[] params) throws ResponseException {
        return switch (cmd) {
            case "logout" -> logout();
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "join" -> joinGame(params);
            case "observe" -> observeGame(params);
            case "quit" -> "quit";
            default -> helpPostLogin();
        };
    }

    private String register(String[] params) throws ResponseException {
        if (params.length == 3) {
            var username = params[0];
            var password = params[1];
            var email = params[2];

            authData = server.register(new UserData(username, password, email));
            state = State.LOGGED_IN;

            return String.format("Successfully registered and logged in as %s.", username);
        }
        return "Expected: <username> <password> <email>";
    }

    private String login(String[] params) throws ResponseException {
        if (params.length == 2) {
            var username = params[0];
            var password = params[1];

            authData = server.login(new UserData(username, password, null));
            state = State.LOGGED_IN;

            return String.format("Successfully logged in as %s.", username);
        }
        return "Expected: <username> <password>";
    }

    private String logout() throws ResponseException {
        server.logout(authData.authToken());

        authData = null;
        state = State.LOGGED_OUT;

        return "Logged out successfully.";
    }

    private String createGame(String[] params) throws ResponseException {
        return "Create game not implemented yet.";
    }

    private String listGames() throws ResponseException {
        return "List games not implemented yet.";
    }

    private String joinGame(String[] params) throws ResponseException {
        return "Join game not implemented yet.";
    }

    private String observeGame(String[] params) throws ResponseException {
        return "Observe game not implemented yet.";
    }

    private String helpPreLogin() {
        return """
                - register <username> <password> <email>
                - login <username> <password>
                - quit
                - help
                """;
    }

    private String helpPostLogin() {
        return """
                create <NAME> - a game
                list - games
                join <ID> [WHITE|BLACK] - a game
                observe <ID> - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """;
    }
}