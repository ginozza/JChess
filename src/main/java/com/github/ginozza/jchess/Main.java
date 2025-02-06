package com.github.ginozza.jchess;

import com.github.ginozza.jchess.model.GamePanel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GamePanel gamePanel = new GamePanel();
        Scene scene = new Scene(gamePanel, GamePanel.WIDTH, GamePanel.HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JChess");
        primaryStage.show();

        gamePanel.launchGame();
    }

}
