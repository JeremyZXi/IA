// src/main/java/com/example/planner/PlannerApp.java
package com.example.planner;

import com.example.planner.data.ConfigManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PlannerApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        if (ConfigManager.settingsExists()) {
            // TODO: load real main UI here
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPlaceholder.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Planner");
            stage.show();
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Onboarding.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Planner • Welcome");
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
