package client;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearData() throws ResponseException {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void registerSuccess() {
        UserData user = new UserData("player1", "password", "p1@email.com");
        assertDoesNotThrow(() -> {
            AuthData auth = facade.register(user);
            assertNotNull(auth.authToken());
        });
    }

    @Test
    public void registerDuplicateUser() throws ResponseException {
        UserData user = new UserData("player1", "password", "p1@email.com");
        facade.register(user);
        ResponseException exception = assertThrows(ResponseException.class, () -> facade.register(user));
        assertEquals(403, exception.statusCode());
    }

    @Test
    public void loginSuccess() throws ResponseException {
        UserData user = new UserData("player1", "password", "p1@email.com");
        facade.register(user);

        assertDoesNotThrow(() -> {
            AuthData auth = facade.login(new UserData("player1", "password", null));
            assertNotNull(auth.authToken());
        });
    }

    @Test
    public void loginWrongPassword() throws ResponseException {
        UserData user = new UserData("player1", "password", "p1@email.com");
        facade.register(user);

        ResponseException exception = assertThrows(ResponseException.class, () ->
                facade.login(new UserData("player1", "wrongpassword", null))
        );
        assertEquals(401, exception.statusCode());
    }

    @Test
    public void logoutSuccess() throws ResponseException {
        UserData user = new UserData("player1", "password", "p1@email.com");
        AuthData auth = facade.register(user);

        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutInvalidToken() {
        ResponseException exception = assertThrows(ResponseException.class, () ->
                facade.logout("fake-or-expired-token")
        );
        assertEquals(401, exception.statusCode());
    }

    @Test
    public void createGameSuccess() throws ResponseException {
        UserData user = new UserData("player1", "password", "p1@email.com");
        AuthData auth = facade.register(user);

        assertDoesNotThrow(() -> {
            int gameId = facade.createGame(auth.authToken(), "My First Game");
            assertTrue(gameId > 0);
        });
    }

    @Test
    public void createGameUnauthorized() {
        ResponseException exception = assertThrows(ResponseException.class, () ->
                facade.createGame("bad-token", "My First Game")
        );
        assertEquals(401, exception.statusCode());
    }

    @Test
    public void listGamesSuccess() throws ResponseException {
        UserData user = new UserData("player1", "password", "p1@email.com");
        AuthData auth = facade.register(user);
        facade.createGame(auth.authToken(), "Game 1");
        facade.createGame(auth.authToken(), "Game 2");

        assertDoesNotThrow(() -> {
            GameData[] games = facade.listGames(auth.authToken());
            assertEquals(2, games.length);
        });
    }

    @Test
    public void listGamesUnauthorized() {
        ResponseException exception = assertThrows(ResponseException.class, () ->
                facade.listGames("bad-token")
        );
        assertEquals(401, exception.statusCode());
    }

    @Test
    public void joinGameSuccess() throws ResponseException {
        UserData user = new UserData("player1", "password", "p1@email.com");
        AuthData auth = facade.register(user);
        int gameId = facade.createGame(auth.authToken(), "Joinable Game");

        assertDoesNotThrow(() ->
                facade.joinGame(auth.authToken(), "WHITE", gameId)
        );
    }

    @Test
    public void joinGameBadId() throws ResponseException {
        UserData user = new UserData("player1", "password", "p1@email.com");
        AuthData auth = facade.register(user);

        ResponseException exception = assertThrows(ResponseException.class, () ->
                facade.joinGame(auth.authToken(), "WHITE", 9999)
        );
        assertEquals(400, exception.statusCode());
    }
}
