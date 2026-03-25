package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.*;
import java.net.*;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public AuthData register(UserData user) throws ResponseException {
        var path = "/user";
        return makeRequest("POST", path, user, AuthData.class, null);
    }

    public AuthData login(UserData user) throws ResponseException {
        var path = "/session";
        return makeRequest("POST", path, user, AuthData.class, null);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        makeRequest("DELETE", path, null, null, authToken);
    }

    public GameData[] listGames(String authToken) throws ResponseException {
        var path = "/game";
        record ListGamesResponse(GameData[] games) {}

        var response = makeRequest("GET", path, null, ListGamesResponse.class, authToken);
        return response != null ? response.games() : new GameData[0];
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        var path = "/game";
        record CreateGameRequest(String gameName) {}
        record CreateGameResponse(int gameID) {}

        var request = new CreateGameRequest(gameName);
        var response = makeRequest("POST", path, request, CreateGameResponse.class, authToken);
        return response.gameID();
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        var path = "/game";
        record JoinGameRequest(String playerColor, int gameID) {}

        var request = new JoinGameRequest(playerColor, gameID);
        makeRequest("PUT", path, request, null, authToken);
    }

    public void clear() throws ResponseException {
        var path = "/db";
        makeRequest("DELETE", path, null, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }

            if (request != null) {
                http.addRequestProperty("Content-Type", "application/json");
                String reqData = new Gson().toJson(request);
                try (OutputStream reqBody = http.getOutputStream()) {
                    reqBody.write(reqData.getBytes());
                }
            }

            http.connect();

            if (http.getResponseCode() >= 400) {
                String errorMessage = "Server Error: " + http.getResponseCode();
                try (InputStream errorStream = http.getErrorStream()) {
                    if (errorStream != null) {
                        record ErrorResponse(String message) {}
                        ErrorResponse error = new Gson().fromJson(new InputStreamReader(errorStream), ErrorResponse.class);
                        if (error != null && error.message() != null) {
                            errorMessage = error.message();
                        }
                    }
                }

                throw new ResponseException(http.getResponseCode(), errorMessage);
            }

            return readBody(http, responseClass);

        } catch (Exception ex) {
            if (ex instanceof ResponseException) {
                throw (ResponseException) ex;
            }
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        if (responseClass == null) {
            return null;
        }
        try (InputStream respBody = http.getInputStream()) {
            InputStreamReader reader = new InputStreamReader(respBody);
            return new Gson().fromJson(reader, responseClass);
        }
    }
}