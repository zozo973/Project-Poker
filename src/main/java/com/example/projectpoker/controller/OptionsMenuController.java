package com.example.projectpoker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;


public class OptionsMenuController {
    private static final int WINDOW_WIDTH = 350;
    private static final int WINDOW_HEIGHT = 400;

    @FXML private Label messageLabel;

    @FXML
    private void goToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/Menu FXML/MainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) (messageLabel).getScene().getWindow();
            Scene optionsScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            stage.setScene(optionsScene);
            stage.setTitle("PokerPro+");
            stage.setMaximized(false);
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Could not load Options Menu.");
        }
    }
}
