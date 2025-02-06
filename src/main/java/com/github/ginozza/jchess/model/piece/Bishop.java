package com.github.ginozza.jchess.model.piece;

import com.github.ginozza.jchess.model.GamePanel;
import com.github.ginozza.jchess.model.Type;

public class Bishop extends Piece {
    public Bishop(int color, int col, int row) {
        super(color, col, row);

        type = Type.KING;

        if(color == GamePanel.WHITE) {
            image = getImage("/piece/wB");
        } else {
            image = getImage("/piece/bB");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBoard(targetCol, targetRow) &&
                isSameSquare(targetCol, targetRow) == false) {
            // Bishop can move diagonally as long as the movement ratio of col and row is 1:1
            if(Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) {
                if(isValidSquare(targetCol, targetRow) &&
                        pieceIsOnDiagonalLine(targetCol, targetRow) == false) {
                    return true;
                }
            }
        }
        return false;
    }
}

