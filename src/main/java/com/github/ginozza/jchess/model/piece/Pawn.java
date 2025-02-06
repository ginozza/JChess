package com.github.ginozza.jchess.model.piece;

import com.github.ginozza.jchess.model.GamePanel;
import com.github.ginozza.jchess.model.Type;

public class Pawn extends Piece {
    public Pawn(int color, int col, int row) {
        super(color, col, row);

        type = Type.PAWN;

        if(color == GamePanel.WHITE) {
            image = getImage("/piece/wP");
        } else {
            image = getImage("/piece/bP");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
            int moveValue;
            if(color == GamePanel.WHITE) {
                moveValue = -1;
            } else {
                moveValue = 1;
            }

            // Check the hitting pieces
            hittingP = getHittingP(targetCol, targetRow);

            // One square movement
            if(targetCol == preCol &&
                    targetRow == preRow + moveValue &&
                    hittingP == null) {
                return true;
            }

            // Two squares movement
            if(targetCol == preCol &&
                    targetRow == preRow + moveValue * 2 &&
                    hittingP == null &&
                    moved == false &&
                    pieceIsOnStraightLine(targetCol, targetRow) == false) {
                return true;
            }

            // Diagonal movement and capture
            if(Math.abs(targetCol - preCol) == 1 &&
                    targetRow == preRow + moveValue &&
                    hittingP != null &&
                    hittingP.color != color) {
                return true;
            }

            // En Passant
            if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
                for(Piece piece : GamePanel.simPieces) {
                    if(piece.col == targetCol &&
                            piece.row == preRow &&
                            piece.twoStepped == true) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
