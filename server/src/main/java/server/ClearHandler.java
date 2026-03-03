package server;

import com.google.gson.Gson;
import service.ClearService;
import io.javalin.http.Context;
import java.util.Map;

public class ClearHandler {
    private final ClearService service;

    public ClearHandler(ClearService service) {
        this.service = service;
    }

    public void handleRequest(Context ctx) {
        try {
            service.clear();
            ctx.status(200);
            ctx.result("{}");
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}