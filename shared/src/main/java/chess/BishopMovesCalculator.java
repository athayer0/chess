package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        bishopMovesHelper(board, myPosition, moves, 1, 1);
        bishopMovesHelper(board, myPosition, moves, -1, 1);
        bishopMovesHelper(board, myPosition, moves, 1, -1);
        bishopMovesHelper(board, myPosition, moves, -1, -1);
        return moves;
    }

    private void bishopMovesHelper(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves,
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
