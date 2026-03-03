package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import chess.ChessGame;

public class ClearServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private ClearService clearService;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        userDAO.createUser(new UserData("testUser", "testPass", "test@email.com"));
        authDAO.createAuth(new AuthData("testToken123", "testUser"));
        gameDAO.createGame(new GameData(1001, "whitePlayer", "blackPlayer", "Test Game", new ChessGame()));

        clearService.clear();

        Assertions.assertNull(userDAO.getUser("testUser"));
        Assertions.assertNull(authDAO.getAuth("testToken123"));
        Assertions.assertTrue(gameDAO.getGames().isEmpty());
    }
}