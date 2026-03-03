package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import chess.ChessGame;

public class GameServiceTests {
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private GameService gameService;

    @BeforeEach
    public void setup() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("testToken", "testUser"));
        CreateGameResult result = gameService.createGame("testToken", new CreateGameRequest("Test Game"));
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(gameDAO.getGame(result.gameID()));
    }

    @Test
    public void createGameFail() {
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            gameService.createGame("badToken", new CreateGameRequest("Test Game"));
        });
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("testToken", "testUser"));
        gameDAO.createGame(new GameData(1, null, null, "Game 1", new ChessGame()));
        gameDAO.createGame(new GameData(2, null, null, "Game 2", new ChessGame()));

        ListGamesResult result = gameService.listGames(new ListGamesRequest("testToken"));
        Assertions.assertEquals(2, result.games().size());
    }

    @Test
    public void listGamesFail() {
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            gameService.listGames(new ListGamesRequest("badToken"));
        });
    }

    @Test
    public void joinGameSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("testToken", "testUser"));
        gameDAO.createGame(new GameData(1, null, null, "Test Game", new ChessGame()));

        gameService.joinGame("testToken", new JoinGameRequest("WHITE", 1));
        Assertions.assertEquals("testUser", gameDAO.getGame(1).whiteUsername());
    }

    @Test
    public void joinGameFail() throws DataAccessException {
        authDAO.createAuth(new AuthData("testToken1", "testUser1"));
        authDAO.createAuth(new AuthData("testToken2", "testUser2"));
        gameDAO.createGame(new GameData(1, "testUser1", null, "Test Game", new ChessGame()));

        Assertions.assertThrows(AlreadyTakenException.class, () -> {
            gameService.joinGame("testToken2", new JoinGameRequest("WHITE", 1));
        });
    }
}
