package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTests {
    private SQLAuthDAO authDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        authDAO.clear();
    }

    @Test
    @DisplayName("Clear Auth - Positive")
    public void clearAuth() throws DataAccessException {
        authDAO.createAuth(new AuthData("token123", "user1"));
        authDAO.clear();
        assertNull(authDAO.getAuth("token123"));
    }

    @Test
    @DisplayName("Create Auth - Positive")
    public void createAuthSuccess() throws DataAccessException {
        AuthData newAuth = new AuthData("token123", "user1");
        authDAO.createAuth(newAuth);

        AuthData retrievedAuth = authDAO.getAuth("token123");
        assertNotNull(retrievedAuth);
        assertEquals("token123", retrievedAuth.authToken());
        assertEquals("user1", retrievedAuth.username());
    }

    @Test
    @DisplayName("Create Auth - Negative (Duplicate Token)")
    public void createAuthFailsOnDuplicate() throws DataAccessException {
        AuthData newAuth = new AuthData("token123", "user1");
        authDAO.createAuth(newAuth);

        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(newAuth);
        });
    }

    @Test
    @DisplayName("Get Auth - Positive")
    public void getAuthSuccess() throws DataAccessException {
        AuthData newAuth = new AuthData("token123", "user1");
        authDAO.createAuth(newAuth);

        AuthData foundAuth = authDAO.getAuth("token123");
        assertNotNull(foundAuth);
        assertEquals("user1", foundAuth.username());
    }

    @Test
    @DisplayName("Get Auth - Negative (Not Found)")
    public void getAuthNotFound() throws DataAccessException {
        AuthData foundAuth = authDAO.getAuth("invalidToken");
        assertNull(foundAuth);
    }

    @Test
    @DisplayName("Delete Auth - Positive")
    public void deleteAuthSuccess() throws DataAccessException {
        AuthData newAuth = new AuthData("token123", "user1");
        authDAO.createAuth(newAuth);

        authDAO.deleteAuth("token123");
        assertNull(authDAO.getAuth("token123"));
    }

    @Test
    @DisplayName("Delete Auth - Negative (Delete Non-existent)")
    public void deleteAuthNonExistent() throws DataAccessException {
        assertDoesNotThrow(() -> {
            authDAO.deleteAuth("fakeToken");
        });
    }
}
