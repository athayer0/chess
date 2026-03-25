package client;

import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGED_OUT;
    private AuthData authData = null;
    private GameData[] cachedGames = null;

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
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
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
        if (params.length >= 1) {
            String gameName = String.join(" ", params);
            server.createGame(authData.authToken(), gameName);
            return String.format("Game '%s' created successfully.", gameName);
        }
        return "Expected: create <NAME>";
    }

    private String listGames() throws ResponseException {
        cachedGames = server.listGames(authData.authToken());

        if (cachedGames == null || cachedGames.length == 0) {
            return "No games currently exist.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Current Games:\n");
        for (int i = 0; i < cachedGames.length; i++) {
            GameData game = cachedGames[i];
            sb.append(String.format(" %d. %s\n", i + 1, game.gameName()));
            sb.append(String.format("    White: %s\n", game.whiteUsername() != null ? game.whiteUsername() : "[Empty]"));
            sb.append(String.format("    Black: %s\n", game.blackUsername() != null ? game.blackUsername() : "[Empty]"));
        }
        return sb.toString();
    }

    private String joinGame(String[] params) throws ResponseException {
        if (params.length == 2) {
            try {
                int gameIndex = Integer.parseInt(params[0]) - 1;
                String color = params[1].toUpperCase();

                if (cachedGames == null || gameIndex < 0 || gameIndex >= cachedGames.length) {
                    return "Invalid game number. Type 'list' to see available games.";
                }

                int actualGameId = cachedGames[gameIndex].gameID();
                server.joinGame(authData.authToken(), color, actualGameId);

                boolean isWhite = color.equals("WHITE");
                ui.BoardPrinter.drawBoard(isWhite);

                return String.format("Successfully joined game %d as %s.", gameIndex + 1, color);

            } catch (NumberFormatException e) {
                return "Expected a number for the game ID.";
            }
        }
        return "Expected: join <ID> [WHITE|BLACK]";
    }

    private String observeGame(String[] params) throws ResponseException {
        if (params.length == 1) {
            try {
                int gameIndex = Integer.parseInt(params[0]) - 1;

                if (cachedGames == null || gameIndex < 0 || gameIndex >= cachedGames.length) {
                    return "Invalid game number. Type 'list' to see available games.";
                }

                ui.BoardPrinter.drawBoard(true);

                return String.format("Now observing game %d.", gameIndex + 1);

            } catch (NumberFormatException e) {
                return "Expected a number for the game ID.";
            }
        }
        return "Expected: observe <ID>";
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