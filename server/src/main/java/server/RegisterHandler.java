package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;
import io.javalin.http.Context;
import java.util.Map;

public class RegisterHandler {
    private final UserService service;

    public RegisterHandler(UserService service) {
        this.service = service;
    }

    public void handleRequest(Context ctx) {
        Gson gson = new Gson();
        try {
            RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResult result = service.register(req);
            ctx.status(200);
            ctx.result(gson.toJson(result));

        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (AlreadyTakenException e) {
            ctx.status(403);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
