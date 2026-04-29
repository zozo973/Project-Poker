package com.example.projectpoker.controller;

import com.example.projectpoker.service.SessionManager;
import com.example.projectpoker.PokerApplication;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class MainMenuController {

    @FXML private Label messageLabel;
    @FXML private Button startGameButton;
    @FXML private Button profileMenuButton;
    @FXML private Button optionsMenuButton;

    @FXML
    private void goToGame(ActionEvent event) {
        try {
            Stage stage = new Stage();
            PokerApplication app = new PokerApplication();
            app.createPokerGame(stage);
        } catch (IOException e) {
            messageLabel.setText("Could not start game: " + e.getMessage());
        }
    }

    @FXML
    private void goToProfile(ActionEvent event) {
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
    private void goToOptions(ActionEvent event) {
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
    private void exitGame(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
}
