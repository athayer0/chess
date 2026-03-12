package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        String createStatement = """
                CREATE TABLE IF NOT EXISTS user (
                  username VARCHAR(255) NOT NULL,
                  password VARCHAR(255) NOT NULL,
                  email VARCHAR(255) NOT NULL,
                  PRIMARY KEY (username)
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
    public void createUser(UserData u) throws DataAccessException {
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        // Hash the password before storing it
        String hashedPassword = BCrypt.hashpw(u.password(), BCrypt.gensalt());

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, u.username());
            ps.setString(2, hashedPassword);
            ps.setString(3, u.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String statement = "SELECT username, password, email FROM user WHERE username=?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE TABLE user";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }
}
