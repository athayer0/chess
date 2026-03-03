package server;

import com.google.gson.Gson;
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
            // 1. Parse JSON from the request body
            LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);

            // 2. Call the service
            LoginResult result = service.login(req);

            // 3. Send success response
            ctx.status(200);
            ctx.result(gson.toJson(result));

        } catch (UnauthorizedException e) {
            ctx.status(401); // 401 Unauthorized
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
