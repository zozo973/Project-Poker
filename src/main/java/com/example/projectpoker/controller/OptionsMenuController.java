package com.example.projectpoker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;


public class OptionsMenuController {
    @FXML private Label messageLabel;

    @FXML
    private void goToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/MainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) (messageLabel).getScene().getWindow();
            Scene optionsScene = new Scene(root);
            stage.setScene(optionsScene);
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Could not load Options Menu.");
        }
    }
}
