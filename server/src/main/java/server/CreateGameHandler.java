package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.GameService;
import io.javalin.http.Context;
import java.util.Map;

public class CreateGameHandler {
    private final GameService service;

    public CreateGameHandler(GameService service) {
        this.service = service;
    }

    public void handleRequest(Context ctx) {
        Gson gson = new Gson();
        try {
            String authToken = ctx.header("authorization");
            CreateGameRequest req = gson.fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResult result = service.createGame(authToken, req);

            ctx.status(200);
            ctx.result(gson.toJson(result));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
