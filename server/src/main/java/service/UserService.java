package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        // 1. Check for bad request (missing fields)
        if (req.username() == null || req.password() == null || req.email() == null) {
            throw new BadRequestException("Error: bad request");
        }

        // 2. Check if username is already taken
        if (userDAO.getUser(req.username()) != null) {
            throw new AlreadyTakenException("Error: already taken");
        }

        // 3. Create and save the new user
        UserData newUser = new UserData(req.username(), req.password(), req.email());
        userDAO.createUser(newUser);

        // 4. Generate an AuthToken and save it
        String authToken = UUID.randomUUID().toString();
        AuthData newAuth = new AuthData(authToken, req.username());
        authDAO.createAuth(newAuth);

        // 5. Return the result
        return new RegisterResult(req.username(), authToken);
    }
}