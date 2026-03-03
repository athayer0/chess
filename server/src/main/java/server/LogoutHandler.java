package server;

import com.google.gson.Gson;
import dataaccess.UnauthorizedException;
import service.LogoutRequest;
import service.UserService;
import io.javalin.http.Context;
import java.util.Map;

public class LogoutHandler {
    private final UserService service;

    public LogoutHandler(UserService service) {
        this.service = service;
    }

    public void handleRequest(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            LogoutRequest req = new LogoutRequest(authToken);
            service.logout(req);
            ctx.status(200);
            ctx.result("{}");

        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(new Gson().toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}