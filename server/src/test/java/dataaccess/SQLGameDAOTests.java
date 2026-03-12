package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;

public class SQLGameDAOTests {
    private SQLGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        gameDAO.clear();
    }

    @Test
    @DisplayName("Clear Games - Positive")
    public void clearGames() throws DataAccessException {
        gameDAO.createGame(new GameData(1, null, null, "Game1", new ChessGame()));
        gameDAO.clear();
        assertEquals(0, gameDAO.getGames().size());
    }

    @Test
    @DisplayName("Create Game - Positive")
    public void createGameSuccess() throws DataAccessException {
        GameData newGame = new GameData(1, null, null, "MyGame", new ChessGame());
        gameDAO.createGame(newGame);

        GameData retrievedGame = gameDAO.getGame(1);
        assertNotNull(retrievedGame);
        assertEquals("MyGame", retrievedGame.gameName());
    }

    @Test
    @DisplayName("Create Game - Negative (Duplicate ID)")
    public void createGameFailsOnDuplicate() throws DataAccessException {
        GameData newGame = new GameData(1, null, null, "MyGame", new ChessGame());
        gameDAO.createGame(newGame);

        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(newGame);
        });
    }

    @Test
    @DisplayName("Get Game - Positive")
    public void getGameSuccess() throws DataAccessException {
        GameData newGame = new GameData(1, "whiteUser", "blackUser", "MyGame", new ChessGame());
        gameDAO.createGame(newGame);

        GameData foundGame = gameDAO.getGame(1);
        assertNotNull(foundGame);
        assertEquals("whiteUser", foundGame.whiteUsername());
    }

    @Test
    @DisplayName("Get Game - Negative (Not Found)")
    public void getGameNotFound() throws DataAccessException {
        GameData foundGame = gameDAO.getGame(999);
        assertNull(foundGame);
    }

    @Test
    @DisplayName("Get Multiple Games - Positive")
    public void getGamesSuccess() throws DataAccessException {
        gameDAO.createGame(new GameData(1, null, null, "Game1", new ChessGame()));
        gameDAO.createGame(new GameData(2, null, null, "Game2", new ChessGame()));

        Collection<GameData> games = gameDAO.getGames();
        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("Get Multiple Games - Negative (Empty)")
    public void getGamesEmpty() throws DataAccessException {
        Collection<GameData> games = gameDAO.getGames();
        assertTrue(games.isEmpty());
    }

    @Test
    @DisplayName("Update Game - Positive")
    public void updateGameSuccess() throws DataAccessException {
        GameData newGame = new GameData(1, null, null, "MyGame", new ChessGame());
        gameDAO.createGame(newGame);

        GameData updatedGame = new GameData(1, "whitePlayer", null, "MyGame", new ChessGame());
        gameDAO.updateGame(updatedGame);

        GameData retrievedGame = gameDAO.getGame(1);
        assertEquals("whitePlayer", retrievedGame.whiteUsername());
    }

    @Test
    @DisplayName("Update Game - Negative (Null Name Constraint)")
    public void updateGameFailsNullName() throws DataAccessException {
        GameData newGame = new GameData(1, null, null, "MyGame", new ChessGame());
        gameDAO.createGame(newGame);

        GameData invalidUpdate = new GameData(1, null, null, null, new ChessGame());
        assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(invalidUpdate);
        });
    }
}
