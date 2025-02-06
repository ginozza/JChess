package com.github.ginozza.jchess.model.piece;

import com.github.ginozza.jchess.model.GamePanel;
import com.github.ginozza.jchess.model.Type;

public class Rook extends Piece {
    public Rook(int color, int col, int row) {
        super(color, col, row);

        type = Type.ROOK;

        if(color == GamePanel.WHITE) {
            image = getImage("/piece/wR");
        } else {
            image = getImage("/piece/bR");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBoard(targetCol, targetRow) &&
                isSameSquare(targetCol, targetRow) == false) {
            // Rook can move as long as either its col or row is the same
            if(targetCol == preCol || targetRow == preRow) {
                if(isValidSquare(targetCol, targetRow) &&
                        pieceIsOnStraightLine(targetCol, targetRow) == false) {
                    return true;
                }
            }
        }

        return false;
    }
}
