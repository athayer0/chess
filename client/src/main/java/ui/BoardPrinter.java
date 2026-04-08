package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BoardPrinter {
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final String[] LETTERS = {"\u3000a ", "\u3000b ", "\u3000c ", "\u3000d ", "\u3000e ", "\u3000f ", "\u3000g ", "\u3000h "};

    public static void drawBoard(ChessGame game, boolean isWhitePerspective) {
        drawBoard(game, isWhitePerspective, Set.of());
    }

    public static void drawBoard(ChessGame game, boolean isWhitePerspective, Collection<ChessPosition> highlights) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        ChessBoard board = game.getBoard();
        Set<ChessPosition> highlightSet = new HashSet<>(highlights);

        out.print(EscapeSequences.ERASE_SCREEN);
        out.println();

        drawHeaders(out, isWhitePerspective);
        drawChessBoard(out, board, isWhitePerspective, highlightSet);
        drawHeaders(out, isWhitePerspective);

        out.print(EscapeSequences.RESET_BG_COLOR);
        out.print(EscapeSequences.RESET_TEXT_COLOR);
        out.println();
    }

    private static void drawHeaders(PrintStream out, boolean isWhitePerspective) {
        out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        out.print("   ");

        for (int boardCol = 1; boardCol <= BOARD_SIZE_IN_SQUARES; boardCol++) {
            int index = isWhitePerspective ? boardCol - 1 : BOARD_SIZE_IN_SQUARES - boardCol;
            out.print(LETTERS[index]);
        }

        out.print("   ");
        out.print(EscapeSequences.RESET_BG_COLOR);
        out.println();
    }

    private static void drawChessBoard(PrintStream out, ChessBoard board, boolean isWhitePerspective,
                                       Set<ChessPosition> highlights) {
        for (int boardRow = 1; boardRow <= BOARD_SIZE_IN_SQUARES; boardRow++) {
            int actualRow = isWhitePerspective ? (BOARD_SIZE_IN_SQUARES - boardRow + 1) : boardRow;

            out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            out.print(" " + actualRow + " ");

            for (int boardCol = 1; boardCol <= BOARD_SIZE_IN_SQUARES; boardCol++) {
                int actualCol = isWhitePerspective ? boardCol : (BOARD_SIZE_IN_SQUARES - boardCol + 1);

                ChessPosition pos = new ChessPosition(actualRow, actualCol);
                boolean isLightSquare = (actualRow + actualCol) % 2 != 0;
                boolean isHighlighted = highlights.contains(pos);

                if (isHighlighted) {
                    out.print(isLightSquare ? EscapeSequences.SET_BG_COLOR_GREEN : EscapeSequences.SET_BG_COLOR_DARK_GREEN);
                } else {
                    out.print(isLightSquare ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_BLACK);
                }

                ChessPiece piece = board.getPiece(pos);
                printPiece(out, piece);
            }

            out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            out.print(" " + actualRow + " ");
            out.print(EscapeSequences.RESET_BG_COLOR);
            out.println();
        }
    }

    private static void printPiece(PrintStream out, ChessPiece piece) {
        if (piece == null) {
            out.print(EscapeSequences.EMPTY);
            return;
        }

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            out.print(EscapeSequences.SET_TEXT_COLOR_RED);
        } else {
            out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
        }

        switch (piece.getPieceType()) {
            case KING -> out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING);
            case QUEEN -> out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN);
            case BISHOP -> out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP);
            case KNIGHT -> out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT);
            case ROOK -> out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK);
            case PAWN -> out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN);
        }
    }
}
