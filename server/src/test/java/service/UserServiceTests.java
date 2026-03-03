package service;

import dataaccess.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void registerSuccess() throws DataAccessException {
        RegisterResult result = userService.register(new RegisterRequest("testUser", "testPass", "test@email.com"));
        Assertions.assertEquals("testUser", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void registerFail() {
        Assertions.assertThrows(AlreadyTakenException.class, () -> {
            userService.register(new RegisterRequest("testUser", "testPass", "test@email.com"));
            userService.register(new RegisterRequest("testUser", "newPass", "new@email.com"));
        });
    }

    @Test
    public void loginSuccess() throws DataAccessException {
        userService.register(new RegisterRequest("testUser", "testPass", "test@email.com"));
        LoginResult result = userService.login(new LoginRequest("testUser", "testPass"));
        Assertions.assertEquals("testUser", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void loginFail() throws DataAccessException {
        userService.register(new RegisterRequest("testUser", "testPass", "test@email.com"));
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            userService.login(new LoginRequest("testUser", "wrongPass"));
        });
    }

    @Test
    public void logoutSuccess() throws DataAccessException {
        RegisterResult regResult = userService.register(new RegisterRequest("testUser", "testPass", "test@email.com"));
        userService.logout(new LogoutRequest(regResult.authToken()));
        Assertions.assertNull(authDAO.getAuth(regResult.authToken()));
    }

    @Test
    public void logoutFail() {
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            userService.logout(new LogoutRequest("fakeToken"));
        });
    }
}
