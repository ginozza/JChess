package com.github.ginozza.jchess.view;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.event.EventHandler;

public class Mouse {
    public int x, y;
    public boolean pressed;

    public EventHandler<MouseEvent> onMousePressed = event -> {
        if (event.getButton() == MouseButton.PRIMARY) {
            pressed = true;
        }
    };

    public EventHandler<MouseEvent> onMouseReleased = event -> {
        if (event.getButton() == MouseButton.PRIMARY) {
            pressed = false;
        }
    };

    public EventHandler<MouseEvent> onMouseDragged = event -> {
        x = (int) event.getX();
        y = (int) event.getY();
    };

    public EventHandler<MouseEvent> onMouseMoved = event -> {
        x = (int) event.getX();
        y = (int) event.getY();
    };
}
