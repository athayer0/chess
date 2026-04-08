package client.websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// We extend Endpoint so this class can handle WebSocket lifecycle events
public class WebSocketFacade extends Endpoint {

    Session session;
    ServerMessageObserver observer;

    public WebSocketFacade(String serverUrl, ServerMessageObserver observer) throws Exception {
        try {
            // Convert http://localhost:8080 to ws://localhost:8080
            serverUrl = serverUrl.replace("http", "ws");
            URI socketURI = new URI(serverUrl + "/ws");
            this.observer = observer;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            // Set up the message handler to listen for incoming server messages
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    Gson gson = new Gson();
                    // 1. Parse the base message to get the type
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

                    // 2. Parse it again into the correct subclass so you have the specific data
                    switch (serverMessage.getServerMessageType()) {
                        case LOAD_GAME -> observer.notify(gson.fromJson(message, LoadGameMessage.class));
                        case NOTIFICATION -> observer.notify(gson.fromJson(message, NotificationMessage.class));
                        case ERROR -> observer.notify(gson.fromJson(message, ErrorMessage.class));
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new Exception("Error connecting to WebSocket: " + ex.getMessage());
        }
    }

    // Required method for the Endpoint interface
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void sendCommand(UserGameCommand command) throws Exception {
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }
}
