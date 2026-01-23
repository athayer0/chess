package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        knightMovesHelper(board, myPosition, moves, 2, 1);
        knightMovesHelper(board, myPosition, moves, 1, 2);
        knightMovesHelper(board, myPosition, moves, -1, 2);
        knightMovesHelper(board, myPosition, moves, -2, 1);
        knightMovesHelper(board, myPosition, moves, -2, -1);
        knightMovesHelper(board, myPosition, moves, -1, -2);
        knightMovesHelper(board, myPosition, moves, 1, -2);
        knightMovesHelper(board, myPosition, moves, 2, -1);
        return moves;
    }

    private void knightMovesHelper(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves,
                                   int verticalChange, int horizontalChange) {
        int row = myPosition.getRow() + horizontalChange;
        int col = myPosition.getColumn() + verticalChange;
        if (board.isOnBoard(row, col)) {
            ChessPosition endPosition = new ChessPosition(row, col);
            ChessPiece target = board.getPiece(endPosition);
            if (target == null) {
                moves.add(new ChessMove(myPosition, endPosition, null));
            } else if (target.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                moves.add(new ChessMove(myPosition, endPosition, null));
            }
        }
    }
}
