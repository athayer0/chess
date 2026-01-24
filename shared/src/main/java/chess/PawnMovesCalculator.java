package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
            pawnMovesHelper(board, myPosition, moves, 1, -1);
            pawnMovesHelper(board, myPosition, moves, 1, 0);
            pawnMovesHelper(board, myPosition, moves, 1, 1);
        } else {
            pawnMovesHelper(board, myPosition, moves, -1, -1);
            pawnMovesHelper(board, myPosition, moves, -1, 0);
            pawnMovesHelper(board, myPosition, moves, -1, 1);
        }

        return moves;
    }

    private void pawnMovesHelper(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves,
                                 int verticalChange, int horizontalChange) {
        int row = myPosition.getRow() + verticalChange;
        int col = myPosition.getColumn() + horizontalChange;
        if (board.isOnBoard(row, col)) {
            ChessPosition endPosition = new ChessPosition(row, col);
            ChessPiece target = board.getPiece(endPosition);
            if (target == null) {
                if (horizontalChange == 0) {
                    if (row == 1 || row == 8) {
                        promotionHelper(myPosition, endPosition, moves);
                    } else {
                        moves.add(new ChessMove(myPosition, endPosition, null));
                    }
                    if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE
                            && myPosition.getRow() == 2 && board.getPiece(new ChessPosition(row+1, col)) == null) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(row+1, col), null));
                    }
                    if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK
                            && myPosition.getRow() == 7 && board.getPiece(new ChessPosition(row-1, col)) == null) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(row+-1, col), null));
                    }
                }
            } else if (target.getTeamColor() != board.getPiece(myPosition).getTeamColor() && horizontalChange != 0) {
                if (row == 1 || row == 8) {
                    promotionHelper(myPosition, endPosition, moves);
                } else {
                    moves.add(new ChessMove(myPosition, endPosition, null));
                }
            }
        }
    }

    private void promotionHelper(ChessPosition myPosition, ChessPosition endPosition, Collection<ChessMove> moves) {
        moves.add(new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT));
        moves.add(new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP));
    }
}
