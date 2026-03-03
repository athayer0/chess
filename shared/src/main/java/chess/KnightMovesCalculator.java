package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static chess.MoveUtils.steppingMovesHelper;

public class KnightMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        steppingMovesHelper(board, myPosition, moves, 2, 1);
        steppingMovesHelper(board, myPosition, moves, 1, 2);
        steppingMovesHelper(board, myPosition, moves, -1, 2);
        steppingMovesHelper(board, myPosition, moves, -2, 1);
        steppingMovesHelper(board, myPosition, moves, -2, -1);
        steppingMovesHelper(board, myPosition, moves, -1, -2);
        steppingMovesHelper(board, myPosition, moves, 1, -2);
        steppingMovesHelper(board, myPosition, moves, 2, -1);
        return moves;
    }
}
