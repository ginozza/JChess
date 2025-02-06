package com.github.ginozza.jchess.model.piece;

import com.github.ginozza.jchess.model.GamePanel;
import com.github.ginozza.jchess.model.Type;

public class Queen extends Piece {
    public Queen(int color, int col, int row) {
        super(color, col, row);

        type = Type.QUEEN;

        if(color == GamePanel.WHITE) {
            image = getImage("/piece/wQ");
        } else {
            image = getImage("/piece/bQ");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
            // Vertical and horizontal
            if(targetCol == preCol || targetRow == preRow) {
                if(isValidSquare(targetCol, targetRow) && pieceIsOnStraightLine(targetCol, targetRow) == false) {
                    return true;
                }
            }
        }
        // Diagonal
        if(Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) {
            if(isValidSquare(targetCol, targetRow) && pieceIsOnDiagonalLine(targetCol, targetRow) == false) {
                return true;
            }
        }
        return false;
    }
}
