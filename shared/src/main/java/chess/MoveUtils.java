package chess;

import java.util.Collection;

public class MoveUtils {

    public static void steppingMovesHelper(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves,
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

    public static void slidingMovesHelper(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves,
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
