package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                ChessPosition endPosition = new ChessPosition(row+i, col+j);
                if (board.isOnBoard(endPosition)) {
                    ChessPiece target = board.getPiece(endPosition);
                    if (target == null) {
                        moves.add(new ChessMove(myPosition, endPosition, null));
                    } else if (target.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                        moves.add(new ChessMove(myPosition, endPosition, null));
                    }
                }
            }
        }
        return moves;
    }
}
