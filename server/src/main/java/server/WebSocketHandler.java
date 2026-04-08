package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

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
            // 1. Add the session to our connection manager
            connections.add(gameID, authToken, session);

            // TODO: Validate that the auth token is real and get the username from the DB
            String username = "TestUser"; // Placeholder for now

            // TODO: Fetch the actual GameData from the DB
            // 2. Send a LOAD_GAME message back to the root client
            // LoadGameMessage loadMessage = new LoadGameMessage(actualGameData);
            // connections.sendMessage(gameID, authToken, loadMessage);

            // 3. Broadcast a NOTIFICATION message to everyone else in the game
            var message = String.format("%s joined the game", username);
            var notification = new websocket.messages.NotificationMessage(message);
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
