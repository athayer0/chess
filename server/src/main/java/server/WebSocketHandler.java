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
        // TODO: Implement make move logic
        System.out.println("Make move command received!");
    }

    private void leave(String authToken, Integer gameID, Session session) {
        // TODO: Implement leave logic
    }

    private void resign(String authToken, Integer gameID, Session session) {
        // TODO: Implement resign logic
    }
}
