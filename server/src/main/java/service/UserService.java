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
        if (req.username() == null || req.password() == null || req.email() == null) {
            throw new BadRequestException("Error: bad request");
        }

        if (userDAO.getUser(req.username()) != null) {
            throw new AlreadyTakenException("Error: already taken");
        }

        UserData newUser = new UserData(req.username(), req.password(), req.email());
        userDAO.createUser(newUser);

        String authToken = UUID.randomUUID().toString();
        AuthData newAuth = new AuthData(authToken, req.username());
        authDAO.createAuth(newAuth);

        return new RegisterResult(req.username(), authToken);
    }

    public LoginResult login(LoginRequest req) throws DataAccessException {
        if (req.username() == null || req.password() == null) {
            throw new BadRequestException("Error: bad request");
        }

        UserData user = userDAO.getUser(req.username());
        if (user == null || !user.password().equals(req.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData newAuth = new AuthData(authToken, req.username());
        authDAO.createAuth(newAuth);

        return new LoginResult(req.username(), authToken);
    }

    public void logout(LogoutRequest req) throws DataAccessException {
        AuthData auth = authDAO.getAuth(req.authToken());
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        authDAO.deleteAuth(req.authToken());
    }
}