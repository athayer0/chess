package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getAuthToken(), command.getGameID(), session);
            case MAKE_MOVE -> {
                MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
                makeMove(moveCommand.getAuthToken(), moveCommand.getGameID(), moveCommand.getMove(), session);
            }
            case LEAVE -> leave(command.getAuthToken(), command.getGameID(), session);
            case RESIGN -> resign(command.getAuthToken(), command.getGameID(), session);
        }
    }

    private void connect(String authToken, Integer gameID, Session session) {
        try {
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: bad auth token")));
                return;
            }

            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: bad game ID")));
                return;
            }

            connections.add(gameID, authToken, session);

            LoadGameMessage loadMessage = new LoadGameMessage(gameData);
            connections.sendMessage(gameID, authToken, loadMessage);

            String message = String.format("%s joined the game.", authData.username());
            NotificationMessage notification = new NotificationMessage(message);
            connections.broadcast(gameID, authToken, notification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeMove(String authToken, Integer gameID, chess.ChessMove move, Session session) {
        try {
            AuthData authData = authDAO.getAuth(authToken);
            GameData gameData = gameDAO.getGame(gameID);

            if (authData == null) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: Bad auth token")));
                return;
            }
            if (gameData == null) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: Bad game ID")));
                return;
            }

            String username = authData.username();
            chess.ChessGame game = gameData.game();

            chess.ChessGame.TeamColor playerColor = null;
            if (username.equals(gameData.whiteUsername())) {
                playerColor = chess.ChessGame.TeamColor.WHITE;
            } else if (username.equals(gameData.blackUsername())) {
                playerColor = chess.ChessGame.TeamColor.BLACK;
            }

            if (playerColor == null) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: Observers cannot make moves")));
                return;
            }
            if (game.getTeamTurn() != playerColor) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: It is not your turn")));
                return;
            }

            if (game.isGameOver()) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: You cannot make a move because the game is already over.")));
                return;
            }

            try {
                game.makeMove(move);
            } catch (chess.InvalidMoveException e) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: Invalid move - " + e.getMessage())));
                return;
            }

            gameDAO.updateGame(gameData);

            LoadGameMessage loadMessage = new LoadGameMessage(gameData);
            connections.broadcast(gameID, null, loadMessage);

            String moveNotification = String.format("%s moved a piece.", username);
            connections.broadcast(gameID, authToken, new NotificationMessage(moveNotification));

            chess.ChessGame.TeamColor opponentColor = (playerColor == chess.ChessGame.TeamColor.WHITE) ?
                    chess.ChessGame.TeamColor.BLACK : chess.ChessGame.TeamColor.WHITE;

            if (game.isInCheckmate(opponentColor)) {
                connections.broadcast(gameID, null, new NotificationMessage(opponentColor + " is in CHECKMATE!"));
            } else if (game.isInCheck(opponentColor)) {
                connections.broadcast(gameID, null, new NotificationMessage(opponentColor + " is in CHECK!"));
            } else if (game.isInStalemate(opponentColor)) {
                connections.broadcast(gameID, null, new NotificationMessage("Game is a STALEMATE!"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: Server error making move")));
            } catch (Exception ignored) {}
        }
    }

    private void leave(String authToken, Integer gameID, Session session) {
        try {
            AuthData authData = authDAO.getAuth(authToken);
            GameData gameData = gameDAO.getGame(gameID);

            if (authData == null || gameData == null) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: Bad request")));
                return;
            }

            String username = authData.username();

            String newWhite = gameData.whiteUsername();
            String newBlack = gameData.blackUsername();

            if (username.equals(newWhite)) {
                newWhite = null;
            } else if (username.equals(newBlack)) {
                newBlack = null;
            }

            GameData updatedGame = new GameData(gameID, newWhite, newBlack, gameData.gameName(), gameData.game());
            gameDAO.updateGame(updatedGame);

            connections.remove(gameID, authToken);

            String message = String.format("%s has left the game.", username);
            connections.broadcast(gameID, authToken, new NotificationMessage(message));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resign(String authToken, Integer gameID, Session session) {
        try {
            AuthData authData = authDAO.getAuth(authToken);
            GameData gameData = gameDAO.getGame(gameID);

            if (authData == null || gameData == null) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: Bad request")));
                return;
            }

            String username = authData.username();

            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: Observers cannot resign")));
                return;
            }

            chess.ChessGame game = gameData.game();
            if (game.isGameOver()) {
                session.getRemote().sendString(gson.toJson(new ErrorMessage("Error: Game is already over")));
                return;
            }

            game.setGameOver(true);
            gameDAO.updateGame(gameData);

            String message = String.format("%s has resigned. The game is over.", username);
            connections.broadcast(gameID, null, new NotificationMessage(message));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
