package com.github.ginozza.jchess.model;

import com.github.ginozza.jchess.controller.GameController;
import com.github.ginozza.jchess.model.piece.*;
import com.github.ginozza.jchess.view.Board;
import com.github.ginozza.jchess.view.Mouse;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Optional;

public class GamePanel extends Pane {
    public static final int WIDTH = 600;
    public static final int HEIGHT = 600;
    private Board board = new Board();
    public Mouse mouse = new Mouse();
    // Pieces
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    private ArrayList<Piece> promoPieces = new ArrayList<>();
    private Piece activeP, checkingP;
    public static Piece castlingP;
    // Colors
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    private int currentColor = WHITE;
    // Booleans
    private boolean canMove;
    private boolean validSquare;
    private boolean promotion;
    private boolean gameover;
    private boolean stalemate;
    private boolean endgameDialogShown = false;

    public Canvas canvas = new Canvas(WIDTH, HEIGHT);
    private GraphicsContext gc = canvas.getGraphicsContext2D();

    private Image gameoverImage;

    public GamePanel() {
        setPrefSize(WIDTH, HEIGHT);
        setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(Color.BLACK, null, null)));
        this.getChildren().add(canvas);

        gameoverImage = new Image(getClass().getResource("/endscreen/gameover.gif").toExternalForm());

        new GameController(this);

        setPieces();
        copyPieces(pieces, simPieces);
    }

    private void update() {
        if (promotion) {
            promoting();
        } else if (!gameover) {
            if (mouse.pressed) {
                if (activeP == null) {
                    for (Piece piece : simPieces) {
                        if (piece.color == currentColor &&
                                piece.col == mouse.x / Board.SQUARE_SIZE &&
                                piece.row == mouse.y / Board.SQUARE_SIZE) {
                            activeP = piece;
                        }
                    }
                } else {
                    simulate();
                }
            }
            if (!mouse.pressed) {
                if (activeP != null) {
                    if (validSquare) {
                        if (activeP.hittingP != null) {
                            int index = activeP.hittingP.getIndex();
                            if (index != -1) {
                                simPieces.remove(index);
                            }
                        }
                        copyPieces(simPieces, pieces);
                        activeP.updatePosition();
                        if (castlingP != null) {
                            checkCastling();
                            castlingP.updatePosition();
                        }
                        if (isKingInCheck() && isCheckmate()) {
                            gameover = true;
                        }
                        else if (!isKingInCheck() && !hasLegalMoves()) {
                            stalemate = true;
                            gameover = true;
                        }
                        else {
                            if (canPromote()) {
                                promotion = true;
                            } else {
                                changePlayer();
                            }
                        }
                    } else {
                        copyPieces(pieces, simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        } else {
            if (!endgameDialogShown) {
                endgameDialogShown = true;
                Platform.runLater(() -> showGameOverDialog());
            }
        }
    }

    private void simulate() {
        canMove = false;
        validSquare = false;
        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);
        if (activeP.canMove(activeP.col, activeP.row)) {
            canMove = true;

            int originalCol = activeP.col;
            int originalRow = activeP.row;
            Piece capturedPiece = null;
            int capturedIndex = -1;

            for (int i = 0; i < simPieces.size(); i++) {
                Piece p = simPieces.get(i);
                if (p != activeP && p.col == activeP.col && p.row == activeP.row) {
                    capturedPiece = p;
                    capturedIndex = i;
                    break;
                }
            }

            if (capturedIndex != -1) {
                simPieces.remove(capturedIndex);
            }

            activeP.col = activeP.col;
            activeP.row = activeP.row;

            boolean kingInCheck = isKingInCheckForColor(currentColor, simPieces);

            activeP.col = originalCol;
            activeP.row = originalRow;
            if (capturedIndex != -1) {
                simPieces.add(capturedIndex, capturedPiece);
            }

            validSquare = !kingInCheck;
        }
    }

    private void changePlayer() {
        if (currentColor == WHITE) {
            currentColor = BLACK;
            for (Piece piece : pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
        } else {
            currentColor = WHITE;
            for (Piece piece : pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }
        activeP = null;
    }

    private void checkCastling() {
        if (castlingP != null) {
            if (castlingP.col == 0) {
                castlingP.col += 3;
            } else if (castlingP.col == 7) {
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    private boolean canPromote() {
        if (activeP.type == Type.PAWN) {
            if ((currentColor == WHITE && activeP.row == 0) ||
                    (currentColor == BLACK && activeP.row == 7)) {
                promoPieces.clear();
                int baseCol = activeP.col;
                int baseRow = activeP.row;
                int offset = (currentColor == WHITE) ? 1 : -1;
                promoPieces.add(new Rook(currentColor, baseCol, baseRow));
                promoPieces.add(new Knight(currentColor, baseCol, baseRow + offset));
                promoPieces.add(new Bishop(currentColor, baseCol, baseRow + 2 * offset));
                promoPieces.add(new Queen(currentColor, baseCol, baseRow + 3 * offset));
                return true;
            }
        }
        return false;
    }

    private void promoting() {
        if (mouse.pressed) {
            for (Piece piece : promoPieces) {
                if (piece.col == mouse.x / Board.SQUARE_SIZE &&
                        piece.row == mouse.y / Board.SQUARE_SIZE) {
                    switch (piece.type) {
                        case ROOK:
                            simPieces.add(new Rook(currentColor, activeP.col, activeP.row));
                            break;
                        case KNIGHT:
                            simPieces.add(new Knight(currentColor, activeP.col, activeP.row));
                            break;
                        case BISHOP:
                            simPieces.add(new Bishop(currentColor, activeP.col, activeP.row));
                            break;
                        case QUEEN:
                            simPieces.add(new Queen(currentColor, activeP.col, activeP.row));
                            break;
                        default:
                            break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces, pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    private boolean isIllegalMove(Piece king) {
        if (king.type == Type.KING) {
            for (Piece piece : simPieces) {
                if (piece != king && piece.color != king.color &&
                        piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing(Piece movingPiece) {
        Piece king = getKing(false);
        for (Piece piece : simPieces) {
            if (piece.color != king.color) {
                if (movingPiece.hittingP != null && piece == movingPiece.hittingP) {
                    continue;
                }
                if (piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isKingInCheck() {
        Piece king = getKing(true);
        if (activeP.canMove(king.col, king.row)) {
            checkingP = activeP;
            return true;
        } else {
            checkingP = null;
        }
        return false;
    }

    private boolean isKingInCheckForColor(int color, ArrayList<Piece> piecesList) {
        Piece king = null;
        for (Piece piece : piecesList) {
            if (piece.type == Type.KING && piece.color == color) {
                king = piece;
                break;
            }
        }
        if (king == null) {
            return true;
        }
        for (Piece piece : piecesList) {
            if (piece.color != color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheckForColor(int color) {
        Piece king = null;
        for (Piece piece : pieces) {
            if (piece.type == Type.KING && piece.color == color) {
                king = piece;
                break;
            }
        }
        if (king == null) {
            return true;
        }
        for (Piece piece : pieces) {
            if (piece.color != color) {
                if (piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMoveLegal(Piece piece, int targetCol, int targetRow) {
        int originalCol = piece.col;
        int originalRow = piece.row;
        Piece captured = null;
        for (Piece other : pieces) {
            if (other != piece && other.col == targetCol && other.row == targetRow) {
                captured = other;
                break;
            }
        }
        piece.col = targetCol;
        piece.row = targetRow;
        if (captured != null) {
            pieces.remove(captured);
        }
        boolean kingSafe = !isKingInCheckForColor(piece.color);
        piece.col = originalCol;
        piece.row = originalRow;
        if (captured != null) {
            pieces.add(captured);
        }
        return kingSafe;
    }

    private boolean hasLegalMoves() {
        for (Piece piece : pieces) {
            if (piece.color == currentColor) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (piece.canMove(col, row) && isMoveLegal(piece, col, row)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isCheckmate() {
        Piece king = getKing(true);
        if (kingCanMove(king)) {
            return false;
        } else {
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);
            if (colDiff == 0) {
                if (checkingP.row < king.row) {
                    for (int row = checkingP.row; row < king.row; row++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.row > king.row) {
                    for (int row = checkingP.row; row > king.row; row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (rowDiff == 0) {
                if (checkingP.col < king.col) {
                    for (int col = checkingP.col; col < king.col; col++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(checkingP.col, col)) {
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.col > king.col) {
                    for (int col = checkingP.col; col > king.col; col--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(checkingP.col, col)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (colDiff == rowDiff) {
                if (checkingP.row < king.row) {
                    if (checkingP.col < king.col) {
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingP.col > king.col) {
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                if (checkingP.row > king.row) {
                    if (checkingP.col < king.col) {
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingP.col > king.col) {
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean kingCanMove(Piece king) {
        if (isValidMove(king, -1, -1)) return true;
        if (isValidMove(king, 0, -1)) return true;
        if (isValidMove(king, 1, -1)) return true;
        if (isValidMove(king, -1, 0)) return true;
        if (isValidMove(king, 1, 0)) return true;
        if (isValidMove(king, -1, 1)) return true;
        if (isValidMove(king, 0, 1)) return true;
        if (isValidMove(king, 1, 1)) return true;
        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus) {
        boolean isValidMove = false;
        king.col += colPlus;
        king.row += rowPlus;
        if (king.canMove(king.col, king.row)) {
            if (king.hittingP != null) {
                simPieces.remove(king.hittingP.getIndex());
            }
            if (!isIllegalMove(king)) {
                isValidMove = true;
            }
        }
        king.resetPosition();
        copyPieces(pieces, simPieces);
        return isValidMove;
    }

    private Piece getKing(boolean opponent) {
        Piece king = null;
        for (Piece piece : simPieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != currentColor) {
                    king = piece;
                }
            } else {
                if (piece.type == Type.KING && piece.color == currentColor) {
                    king = piece;
                }
            }
        }
        return king;
    }

    public void paintComponent() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        board.draw(gc);
        for (Piece p : simPieces) {
            p.draw(gc);
        }
        if (activeP != null) {
            if (canMove) {
                if (!isIllegalMove(activeP) && !opponentCanCaptureKing(activeP)) {
                    gc.setFill(Color.WHITE);
                    gc.setGlobalAlpha(0.7f);
                    gc.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    gc.setGlobalAlpha(1f);
                }
                activeP.draw(gc);
            }
        }
        if (promotion) {
            for (Piece piece : promoPieces) {
                gc.setFill(Color.WHITE);
                gc.fillRect(piece.col * Board.SQUARE_SIZE, piece.row * Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE);
            }
            for (Piece piece : promoPieces) {
                gc.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row),
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE);
            }
        }
        if (gameover) {
            double imgWidth = gameoverImage.getWidth();
            double imgHeight = gameoverImage.getHeight();
            double x = (WIDTH - imgWidth) / 2;
            double y = (HEIGHT - imgHeight) / 2;
            gc.drawImage(gameoverImage, x, y);
        }
    }

    public void launchGame() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                paintComponent();
            }
        };
        timer.start();
    }

    public void setPieces() {
        pieces.clear();

        // White
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));

        // Black
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();
        for (Piece piece : source) {
            target.add(piece);
        }
    }

    private void showGameOverDialog() {
        ButtonType btnSi = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.NO);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Deseas volver a jugar?", btnSi, btnNo);
        alert.setTitle("Juego Terminado");
        alert.setHeaderText(stalemate ? "Stalemate!" : "¡Checkmate!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnSi) {
            resetGame();
        } else {
            Platform.exit();
        }
    }

    private void resetGame() {
        pieces.clear();
        simPieces.clear();
        setPieces();
        copyPieces(pieces, simPieces);
        currentColor = WHITE;
        activeP = null;
        promotion = false;
        gameover = false;
        stalemate = false;
        endgameDialogShown = false;
    }
}
