package server;

import com.google.gson.Gson;
import dataaccess.UnauthorizedException;
import service.GameService;
import service.ListGamesRequest;
import service.ListGamesResult;
import io.javalin.http.Context;
import java.util.Map;

public class ListGamesHandler {
    private final GameService service;

    public ListGamesHandler(GameService service) {
        this.service = service;
    }

    public void handleRequest(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            ListGamesRequest req = new ListGamesRequest(authToken);
            ListGamesResult result = service.listGames(req);

            ctx.status(200);
            ctx.result(new Gson().toJson(result));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(new Gson().toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
