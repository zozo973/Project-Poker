package com.example.projectpoker.controller;

import com.example.projectpoker.PokerApplication;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class MainMenuController {

    @FXML private Label messageLabel;

    @FXML
    private void goToGame() {
        try {
            Stage stage = new Stage();
            PokerApplication app = new PokerApplication();
            app.createPokerGame(stage);
        } catch (IOException e) {
            messageLabel.setText("Could not start game: " + e.getMessage());
        }
    }

    @FXML
    private void goToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/Account & Profile UI/profile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            Scene ProfileScene = new Scene(root);
            stage.setScene(ProfileScene);
            stage.show();

        } catch (IOException e) {
            messageLabel.setText("Could not load Profile Menu.");
        }
    }

    @FXML
    private void goToOptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/OptionsMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) (messageLabel).getScene().getWindow();
            Scene optionsScene = new Scene(root);
            stage.setScene(optionsScene);
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Could not load Options Menu.");
        }
    }

    @FXML
    private void exitGame() {
        Platform.exit();
        System.exit(0);
    }
}
