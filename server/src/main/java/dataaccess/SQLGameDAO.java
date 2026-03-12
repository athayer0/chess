package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO implements GameDAO {

    public SQLGameDAO() throws DataAccessException {
        String createStatement = """
                CREATE TABLE IF NOT EXISTS game (
                  gameID INT NOT NULL,
                  whiteUsername VARCHAR(255),
                  blackUsername VARCHAR(255),
                  gameName VARCHAR(255) NOT NULL,
                  gameData TEXT NOT NULL,
                  PRIMARY KEY (gameID)
                )
                """;
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(createStatement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to configure database: %s", e.getMessage()));
        }
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {
        String statement = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, gameData) VALUES (?, ?, ?, ?, ?)";
        String gameJson = new Gson().toJson(gameData.game());

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setInt(1, gameData.gameID());
            ps.setString(2, gameData.whiteUsername());
            ps.setString(3, gameData.blackUsername());
            ps.setString(4, gameData.gameName());
            ps.setString(5, gameJson);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, gameData FROM game WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    String whiteUser = rs.getString("whiteUsername");
                    String blackUser = rs.getString("blackUsername");
                    String name = rs.getString("gameName");
                    String gameJson = rs.getString("gameData");

                    ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);
                    return new GameData(gameID, whiteUser, blackUser, name, game);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Collection<GameData> getGames() throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, gameData FROM game";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                int gameID = rs.getInt("gameID");
                String whiteUser = rs.getString("whiteUsername");
                String blackUser = rs.getString("blackUsername");
                String name = rs.getString("gameName");
                String gameJson = rs.getString("gameData");

                ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);
                games.add(new GameData(gameID, whiteUser, blackUser, name, game));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        String statement = "UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, gameData=? WHERE gameID=?";
        String gameJson = new Gson().toJson(gameData.game());
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, gameData.whiteUsername());
            ps.setString(2, gameData.blackUsername());
            ps.setString(3, gameData.gameName());
            ps.setString(4, gameJson);
            ps.setInt(5, gameData.gameID());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE TABLE game";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }
}