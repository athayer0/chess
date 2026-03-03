package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import service.*;
import io.javalin.http.Context;
import java.util.Map;

public class GameHandler {
    private final GameService service;

    public GameHandler(GameService service) {
        this.service = service;
    }

    public void handleListGames(Context ctx) {
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

    public void handleCreateGame(Context ctx) {
        Gson gson = new Gson();
        try {
            String authToken = ctx.header("authorization");
            CreateGameRequest req = gson.fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResult result = service.createGame(authToken, req);
            ctx.status(200);
            ctx.result(gson.toJson(result));

        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
