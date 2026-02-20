package com.dopamine.cobblemontrainermaker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class TrainerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TrainerApplication.class.getResource("app-view.fxml"));
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(fxmlLoader.load(), 0.8*bounds.getWidth(), 0.8*bounds.getHeight());
        stage.setX(0.125*bounds.getMaxX());
        stage.setY(0.125*bounds.getMaxY());
        stage.setTitle("Cobblemon Trainer Maker");
        stage.setScene(scene);
        stage.show();
    }
}
