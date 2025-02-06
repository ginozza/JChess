package com.github.ginozza.jchess.controller;

import com.github.ginozza.jchess.model.GamePanel;
import javafx.scene.input.MouseEvent;

public class GameController {
    private GamePanel gamePanel;

    public GameController(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setupMouseEvents();
    }

    private void setupMouseEvents() {
        gamePanel.canvas.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            gamePanel.mouse.x = (int) event.getX();
            gamePanel.mouse.y = (int) event.getY();
        });
        gamePanel.canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            gamePanel.mouse.x = (int) event.getX();
            gamePanel.mouse.y = (int) event.getY();
        });

        gamePanel.canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            gamePanel.mouse.pressed = true;
        });

        gamePanel.canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            gamePanel.mouse.pressed = false;
        });
    }
}