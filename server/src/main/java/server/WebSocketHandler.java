package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

@WebSocket
public class WebSocketHandler {

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
        // TODO: Validate auth token, check if user is in game
        // TODO: Save connection so we can send messages back to them later
        // TODO: Send LOAD_GAME back to the user
        // TODO: Send NOTIFICATION to everyone else in the game
        System.out.println("Connect command received!");
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
