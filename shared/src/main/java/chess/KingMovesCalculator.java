package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        kingMovesHelper(board, myPosition, moves, 1, 1);
        kingMovesHelper(board, myPosition, moves, -1, 1);
        kingMovesHelper(board, myPosition, moves, 1, -1);
        kingMovesHelper(board, myPosition, moves, -1, -1);
        kingMovesHelper(board, myPosition, moves, 1, 0);
        kingMovesHelper(board, myPosition, moves, 0, 1);
        kingMovesHelper(board, myPosition, moves, -1, 0);
        kingMovesHelper(board, myPosition, moves, 0, -1);
        return moves;
    }

    private void kingMovesHelper(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves,
                                  int verticalChange, int horizontalChange) {
        int row = myPosition.getRow() + verticalChange;
        int col = myPosition.getColumn() + horizontalChange;
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