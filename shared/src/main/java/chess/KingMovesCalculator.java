package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static chess.MoveUtils.steppingMovesHelper;

public class KingMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        steppingMovesHelper(board, myPosition, moves, 1, 1);
        steppingMovesHelper(board, myPosition, moves, -1, 1);
        steppingMovesHelper(board, myPosition, moves, 1, -1);
        steppingMovesHelper(board, myPosition, moves, -1, -1);
        steppingMovesHelper(board, myPosition, moves, 1, 0);
        steppingMovesHelper(board, myPosition, moves, 0, 1);
        steppingMovesHelper(board, myPosition, moves, -1, 0);
        steppingMovesHelper(board, myPosition, moves, 0, -1);
        return moves;
    }
}