package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, String authToken, Session session) {
        connections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>()).put(authToken, session);
    }

    public void remove(int gameID, String authToken) {
        if (connections.containsKey(gameID)) {
            connections.get(gameID).remove(authToken);
            if (connections.get(gameID).isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, String excludeAuthToken, ServerMessage message) throws IOException {
        ConcurrentHashMap<String, Session> gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            var removeList = new ArrayList<String>();
            for (var c : gameConnections.entrySet()) {
                if (!c.getKey().equals(excludeAuthToken)) {
                    if (c.getValue().isOpen()) {
                        c.getValue().getRemote().sendString(new Gson().toJson(message));
                    } else {
                        removeList.add(c.getKey());
                    }
                }
            }

            for (String c : removeList) {
                gameConnections.remove(c);
            }
        }
    }

    public void sendMessage(int gameID, String authToken, ServerMessage message) throws IOException {
        ConcurrentHashMap<String, Session> gameConnections = connections.get(gameID);
        if (gameConnections != null && gameConnections.containsKey(authToken)) {
            Session session = gameConnections.get(authToken);
            if (session.isOpen()) {
                session.getRemote().sendString(new Gson().toJson(message));
            }
        }
    }
}
