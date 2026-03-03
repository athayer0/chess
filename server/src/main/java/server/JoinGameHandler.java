package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import service.GameService;
import service.JoinGameRequest;
import io.javalin.http.Context;
import java.util.Map;

public class JoinGameHandler {
    private final GameService service;

    public JoinGameHandler(GameService service) {
        this.service = service;
    }

    public void handleRequest(Context ctx) {
        Gson gson = new Gson();
        try {
            String authToken = ctx.header("authorization");
            JoinGameRequest req = gson.fromJson(ctx.body(), JoinGameRequest.class);
            service.joinGame(authToken, req);

            ctx.status(200);
            ctx.result("{}");
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (AlreadyTakenException e) {
            ctx.status(403);
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
