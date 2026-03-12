package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTests {
    private SQLUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new SQLUserDAO();
        userDAO.clear();
    }

    @Test
    @DisplayName("Clear Users - Positive")
    public void clearUsers() throws DataAccessException {
        userDAO.createUser(new UserData("test1", "pass", "email"));
        userDAO.clear();
        assertNull(userDAO.getUser("test1"));
    }

    @Test
    @DisplayName("Create User - Positive")
    public void createUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("testUser", "testPass", "test@email.com");
        userDAO.createUser(newUser);

        UserData retrievedUser = userDAO.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals("testUser", retrievedUser.username());
        assertEquals("test@email.com", retrievedUser.email());
    }

    @Test
    @DisplayName("Create User - Negative (Duplicate Username)")
    public void createUserFailsOnDuplicate() throws DataAccessException {
        UserData newUser = new UserData("testUser", "testPass", "test@email.com");
        userDAO.createUser(newUser);

        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(newUser);
        });
    }

    @Test
    @DisplayName("Get User - Positive")
    public void getUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("findMe", "pass123", "find@email.com");
        userDAO.createUser(newUser);

        UserData foundUser = userDAO.getUser("findMe");
        assertNotNull(foundUser);
        assertEquals(newUser.username(), foundUser.username());
    }

    @Test
    @DisplayName("Get User - Negative (Not Found)")
    public void getUserNotFound() throws DataAccessException {
        UserData foundUser = userDAO.getUser("nobody");
        assertNull(foundUser);
    }
}
