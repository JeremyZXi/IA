// src/main/java/com/example/planner/PlannerApp.java
package com.example.planner;

import com.example.planner.data.ConfigManager;
import com.example.planner.ui.PlannerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PlannerApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        if (ConfigManager.settingsExists()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPlaceholder.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Planner");

            PlannerController controller = loader.getController();
            stage.setOnCloseRequest(event -> {
                try {
                    controller.shutdown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            stage.show();

        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Onboarding.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Planner - Welcome");
            stage.show();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
