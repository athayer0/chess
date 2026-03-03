package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import service.LoginRequest;
import service.LoginResult;
import service.UserService;
import io.javalin.http.Context;
import java.util.Map;

public class LoginHandler {
    private final UserService service;

    public LoginHandler(UserService service) {
        this.service = service;
    }

    public void handleRequest(Context ctx) {
        Gson gson = new Gson();
        try {
            LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
            LoginResult result = service.login(req);
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
