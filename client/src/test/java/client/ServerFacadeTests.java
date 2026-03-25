package client;

import model.AuthData;
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
            assertNotNull(auth);
            assertNotNull(auth.authToken(), "Auth token should not be null after successful registration");
            assertEquals("player1", auth.username(), "Username returned should match the requested username");
        });
    }

    @Test
    public void registerDuplicateUser() throws ResponseException {
        UserData user = new UserData("player1", "password", "p1@email.com");
        facade.register(user);
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            facade.register(user);
        });
        assertEquals(403, exception.statusCode(), "Expected a 403 status code for duplicate registration");
    }
}
