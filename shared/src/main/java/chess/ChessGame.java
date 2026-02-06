package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    ChessBoard chessBoard = new ChessBoard();
    TeamColor teamColor;
    public ChessGame() {
        chessBoard.resetBoard();
        teamColor = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        return piece.pieceMoves(chessBoard, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        chessBoard.addPiece(endPosition, chessBoard.getPiece(startPosition));
    }

    /**
     * Determines if the given team is in check
     *
     * @param team which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor team) {
        ChessPosition kingPosition = kingFinder(team);
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition square = new ChessPosition(i, j);
                ChessPiece piece = chessBoard.getPiece(square);
                if (piece != null && piece.getTeamColor() != team) {
                    Collection<ChessMove> moves = piece.pieceMoves(chessBoard, square);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition() == kingPosition) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param team which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor team) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param team which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor team) {
        throw new RuntimeException("Not implemented");
    }

    private ChessPosition kingFinder(TeamColor team) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition square = new ChessPosition(i, j);
                ChessPiece piece = chessBoard.getPiece(square);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == team) {
                    return square;
                }
            }
        }
        throw new RuntimeException("couldnt find king");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(chessBoard, chessGame.chessBoard) && teamColor == chessGame.teamColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chessBoard, teamColor);
    }
}
