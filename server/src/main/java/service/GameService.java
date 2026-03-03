package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public ListGamesResult listGames(ListGamesRequest req) throws DataAccessException {
        AuthData auth = authDAO.getAuth(req.authToken());
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        Collection<GameData> games = gameDAO.getGames();

        return new ListGamesResult(games);
    }

    // You may need to import chess.ChessGame at the top of the file!

    public CreateGameResult createGame(String authToken, CreateGameRequest req) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        if (req.gameName() == null || req.gameName().isEmpty()) {
            throw new BadRequestException("Error: bad request");
        }

        int newGameId = Math.abs(java.util.UUID.randomUUID().hashCode());
        GameData newGame = new GameData(newGameId, null, null, req.gameName(), new chess.ChessGame());
        gameDAO.createGame(newGame);

        return new CreateGameResult(newGameId);
    }
}