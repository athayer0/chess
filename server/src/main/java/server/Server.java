package server;

import dataaccess.*;
import io.javalin.*;
import service.ClearService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        // 1. Instantiate the Memory DAOs
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        // 2. Instantiate the Service (injecting the DAOs)
        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);

        // 3. Instantiate the Handler (injecting the Service)
        ClearHandler clearHandler = new ClearHandler(clearService);

        // 4. Register the endpoint routes using Javalin
        javalin.delete("/db", clearHandler::handleRequest);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
