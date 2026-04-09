package client;

import client.websocket.ServerMessageObserver;
import client.websocket.WebSocketFacade;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import model.UserData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ChessClient implements ServerMessageObserver {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGED_OUT;
    private AuthData authData = null;
    private GameData[] cachedGames = null;

    private WebSocketFacade ws;
    private int currentGameId = -1;
    private boolean isWhite = true;
    private ChessGame currentGame = null;

    public enum State {
        LOGGED_OUT,
        LOGGED_IN,
        IN_GAME
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
            var tokens = input.trim().split("\\s+");
            var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (state) {
                case LOGGED_OUT -> evalPreLogin(cmd, params);
                case LOGGED_IN -> evalPostLogin(cmd, params);
                case IN_GAME -> evalInGame(cmd, params);
            };
        } catch (Exception ex) {
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

    private String evalInGame(String cmd, String[] params) throws Exception {
        return switch (cmd) {
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame();
            case "move" -> makeMove(params);
            case "resign" -> resignGame();
            case "highlight" -> highlightMoves(params);
            default -> helpInGame();
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
            listGames();
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

                currentGameId = cachedGames[gameIndex].gameID();

                server.joinGame(authData.authToken(), color, currentGameId);
                isWhite = color.equals("WHITE");

                ws = new WebSocketFacade(serverUrl, this);
                ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authData.authToken(), currentGameId));

                state = State.IN_GAME;

                return String.format("Successfully joined game %d as %s.", gameIndex + 1, color);

            } catch (NumberFormatException e) {
                return "Expected a number for the game ID.";
            } catch (Exception e) {
                return "Failed to connect to WebSocket: " + e.getMessage();
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

                currentGameId = cachedGames[gameIndex].gameID();
                isWhite = true;

                ws = new WebSocketFacade(serverUrl, this);
                ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authData.authToken(), currentGameId));

                state = State.IN_GAME;

                return String.format("Now observing game %d.", gameIndex + 1);

            } catch (NumberFormatException e) {
                return "Expected a number for the game ID.";
            } catch (Exception e) {
                return "Failed to connect to WebSocket: " + e.getMessage();
            }
        }
        return "Expected: observe <ID>";
    }

    private String redrawBoard() {
        if (currentGame != null) {
            ui.BoardPrinter.drawBoard(currentGame, isWhite);
            return "";
        }
        return "No game data available to redraw.";
    }

    private String leaveGame() throws Exception {
        ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authData.authToken(), currentGameId));
        state = State.LOGGED_IN;
        currentGameId = -1;
        currentGame = null;
        return "You have left the game.";
    }

    private String resignGame() throws Exception {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String response = scanner.nextLine().trim();

        if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
            ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authData.authToken(), currentGameId));
            return "Resignation request sent.";
        }

        return "Resignation cancelled.";
    }

    private String makeMove(String[] params) throws Exception {
        if (params.length < 2) {
            return "Expected: move <START> <END> (e.g., move e2 e4)";
        }

        if (params.length > 3) {
            return "Too many arguments. Expected: move <START> <END> or move <START> <END> <PROMOTION>";
        }

        String startPos = params[0].toLowerCase();
        String endPos = params[1].toLowerCase();
        chess.ChessPiece.PieceType promotionPiece = null;

        if (params.length == 3) {
            switch (params[2].toLowerCase()) {
                case "queen" -> promotionPiece = chess.ChessPiece.PieceType.QUEEN;
                case "rook" -> promotionPiece = chess.ChessPiece.PieceType.ROOK;
                case "bishop" -> promotionPiece = chess.ChessPiece.PieceType.BISHOP;
                case "knight" -> promotionPiece = chess.ChessPiece.PieceType.KNIGHT;
                default -> {
                    return "Invalid promotion piece '" + params[2] + "'. Choose queen, rook, bishop, or knight.";
                }
            }
        }

        try {
            int startCol = startPos.charAt(0) - 'a' + 1;
            int startRow = Character.getNumericValue(startPos.charAt(1));
            int endCol = endPos.charAt(0) - 'a' + 1;
            int endRow = Character.getNumericValue(endPos.charAt(1));

            ChessPosition start = new ChessPosition(startRow, startCol);
            ChessPosition end = new ChessPosition(endRow, endCol);

            ChessMove move = new ChessMove(start, end, promotionPiece);

            MakeMoveCommand command = new MakeMoveCommand(authData.authToken(), currentGameId, move);
            ws.sendCommand(command);

            return "Move sent to server...";

        } catch (Exception e) {
            if (params.length == 3) {
                return "Invalid move format. Try something like: 'move e7 e8 queen'";
            } else {
                return "Invalid move format. Try something like: 'move e2 e4'";
            }
        }
    }

    private String highlightMoves(String[] params) {
        if (currentGame == null) {
            return "No game data available.";
        }
        if (params.length < 1) {
            return "Expected: highlight <POSITION> (e.g., highlight e2)";
        }

        String posStr = params[0].toLowerCase();
        try {
            int col = posStr.charAt(0) - 'a' + 1;
            int row = Character.getNumericValue(posStr.charAt(1));

            ChessPosition position = new ChessPosition(row, col);
            Collection<ChessMove> validMoves = currentGame.validMoves(position);

            Collection<ChessPosition> highlights = new ArrayList<>();
            highlights.add(position);
            for (ChessMove move : validMoves) {
                highlights.add(move.getEndPosition());
            }

            ui.BoardPrinter.drawBoard(currentGame, isWhite, highlights);
            return "";
        } catch (Exception e) {
            return "Invalid position. Try something like: 'highlight e2'";
        }
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> {
                NotificationMessage notification = (NotificationMessage) message;
                System.out.println("\n" + notification.getMessage());
                printPrompt();
            }
            case ERROR -> {
                ErrorMessage error = (ErrorMessage) message;
                System.out.println("\n" + error.getErrorMessage());
                printPrompt();
            }
            case LOAD_GAME -> {
                LoadGameMessage load = (LoadGameMessage) message;
                this.currentGame = load.getGame().game();

                System.out.println();
                redrawBoard();
                printPrompt();
            }
        }
    }

    private void printPrompt() {
        if (state == State.IN_GAME) {
            System.out.print("\n[IN_GAME] >>> ");
        } else {
            System.out.print("\n[LOGGED_IN] >>> ");
        }
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

    private String helpInGame() {
        return """
                redraw - Redraw the chess board
                move <START> <END> - Make a move (e.g., move e2 e4)
                highlight <POSITION> - Highlight legal moves for a piece (e.g., highlight e2)
                leave - Leave the game and return to the main menu
                resign - Forfeit the game
                help - Show this menu
                """;
    }
}