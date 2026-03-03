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

    public void joinGame(String authToken, JoinGameRequest req) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(req.gameID());
        if (game == null) {
            throw new BadRequestException("Error: bad request");
        }

        String username = auth.username();
        String newWhite = game.whiteUsername();
        String newBlack = game.blackUsername();

        if ("WHITE".equals(req.playerColor())) {
            if (newWhite != null) {
                throw new AlreadyTakenException("Error: already taken");
            }
            newWhite = username;
        } else if ("BLACK".equals(req.playerColor())) {
            if (newBlack != null) {
                throw new AlreadyTakenException("Error: already taken");
            }
            newBlack = username;
        } else {
            throw new BadRequestException("Error: bad request");
        }

        GameData updatedGame = new GameData(
                game.gameID(),
                newWhite,
                newBlack,
                game.gameName(),
                game.game()
        );

        gameDAO.updateGame(updatedGame);
    }
}