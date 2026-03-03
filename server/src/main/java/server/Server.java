package server;

import dataaccess.*;
import io.javalin.*;
import service.ClearService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);

        ClearHandler clearHandler = new ClearHandler(clearService);
        javalin.delete("/db", clearHandler::handleRequest);

        UserService userService = new UserService(userDAO, authDAO);

        RegisterHandler registerHandler = new RegisterHandler(userService);
        javalin.post("/user", registerHandler::handleRequest);

        LoginHandler loginHandler = new LoginHandler(userService);
        javalin.post("/session", loginHandler::handleRequest);

        LogoutHandler logoutHandler = new LogoutHandler(userService);
        javalin.delete("/session", logoutHandler::handleRequest);

        GameService gameService = new GameService(gameDAO, authDAO);

        GameHandler gameHandler = new GameHandler(gameService);
        javalin.get("/game", gameHandler::handleListGames);
        javalin.post("/game", gameHandler::handleCreateGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
