package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        while (row < 8 && col < 8) {
            ChessPosition endPosition = new ChessPosition(row+1, col+1);
            if (board.getPiece(endPosition) != null) {
                if (board.getPiece(endPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, endPosition, null));
                }
                break;
            }
            moves.add(new ChessMove(myPosition, endPosition, null));
            row++;
            col++;
        }

        row = myPosition.getRow();
        col = myPosition.getColumn();
        while (row > 1 && col < 8) {
            ChessPosition endPosition = new ChessPosition(row-1, col+1);
            if (board.getPiece(endPosition) != null) {
                if (board.getPiece(endPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, endPosition, null));
                }
                break;
            }
            moves.add(new ChessMove(myPosition, endPosition, null));
            row--;
            col++;
        }

        row = myPosition.getRow();
        col = myPosition.getColumn();
        while (row > 1 && col > 1) {
            ChessPosition endPosition = new ChessPosition(row-1, col-1);
            if (board.getPiece(endPosition) != null) {
                if (board.getPiece(endPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, endPosition, null));
                }
                break;
            }
            moves.add(new ChessMove(myPosition, endPosition, null));
            row--;
            col--;
        }

        row = myPosition.getRow();
        col = myPosition.getColumn();
        while (row < 8 && col > 1) {
            ChessPosition endPosition = new ChessPosition(row+1, col-1);
            if (board.getPiece(endPosition) != null) {
                if (board.getPiece(endPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, endPosition, null));
                }
                break;
            }
            moves.add(new ChessMove(myPosition, endPosition, null));
            row++;
            col--;
        }

        return moves;
    }
}
