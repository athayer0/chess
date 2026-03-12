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

        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to start database", e);
        }

        UserDAO userDAO;
        AuthDAO authDAO;
        GameDAO gameDAO;

        try {
            userDAO = new SQLUserDAO();
            authDAO = new SQLAuthDAO();
            gameDAO = new SQLGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to initialize DAOs", e);
        }

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

        ListGamesHandler listGamesHandler = new ListGamesHandler(gameService);
        javalin.get("/game", listGamesHandler::handleRequest);

        CreateGameHandler createGameHandler = new CreateGameHandler(gameService);
        javalin.post("/game", createGameHandler::handleRequest);

        JoinGameHandler joinGameHandler = new JoinGameHandler(gameService);
        javalin.put("/game", joinGameHandler::handleRequest);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
