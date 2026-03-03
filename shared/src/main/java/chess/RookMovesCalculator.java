package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static chess.MoveUtils.slidingMovesHelper;

public class RookMovesCalculator implements PieceMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        slidingMovesHelper(board, myPosition, moves, 1, 0);
        slidingMovesHelper(board, myPosition, moves, 0, 1);
        slidingMovesHelper(board, myPosition, moves, -1, 0);
        slidingMovesHelper(board, myPosition, moves, 0, -1);
        return moves;
    }
}
