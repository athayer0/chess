package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMovesCalculator implements PieceMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        rookMovesHelper(board, myPosition, moves, 1, 0);
        rookMovesHelper(board, myPosition, moves, 0, 1);
        rookMovesHelper(board, myPosition, moves, -1, 0);
        rookMovesHelper(board, myPosition, moves, 0, -1);
        return moves;
    }

    private void rookMovesHelper(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves,
                                 int verticalChange, int horizontalChange) {
        int row = myPosition.getRow() + verticalChange;
        int col = myPosition.getColumn() + horizontalChange;
        while (board.isOnBoard(row, col)) {
            ChessPosition endPosition = new ChessPosition(row, col);
            ChessPiece target = board.getPiece(endPosition);
            if (target != null) {
                if (target.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, endPosition, null));
                }
                break;
            }
            moves.add(new ChessMove(myPosition, endPosition, null));
            row += verticalChange;
            col += horizontalChange;
        }
    }
}
