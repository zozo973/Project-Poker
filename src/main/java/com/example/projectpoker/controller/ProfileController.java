package com.example.projectpoker.controller;

import com.example.projectpoker.service.SessionManager;
import com.example.projectpoker.model.User;
import com.example.projectpoker.database.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class ProfileController {

    @FXML private Label usernameLabel;
    @FXML private Label totalHandsLabel;
    @FXML private Label totalWinsLabel;
    @FXML private Label balanceLabel;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();

        if (user == null) {
            messageLabel.setText("No user logged in.");
            return;
        }

        usernameLabel.setText(user.getUsername());
        totalHandsLabel.setText(Integer.toString(user.getTotalHandsPlayed()));
        totalWinsLabel.setText(Integer.toString(user.getTotalWins()));
        balanceLabel.setText(Integer.toString(user.getCurrentBalance()));
    }

    @FXML
    private void handleBuyIn() {
        User user = SessionManager.getCurrentUser();

        if (user == null) {
            messageLabel.setText("No user logged in.");
            return;
        }

        // Add 1000 to the user's balance
        user.setCurrentBalance(user.getCurrentBalance() + 1000);

        // Persist to database
        new UserDAO().update(user);

        // Update the UI
        balanceLabel.setText(Integer.toString(user.getCurrentBalance()));
//        messageLabel.setText("Successfully added $1000! New balance: $" + user.getCurrentBalance());
    }

    @FXML
    private void goToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/MainMenu.fxml"));
            Parent root = loader.load();
            messageLabel.getScene().setRoot(root);
        } catch (IOException e) {
            messageLabel.setText("Could not load Main Menu.");
        }
    }

    @FXML
    private void logOut() {
        SessionManager.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/Account & Profile UI/login.fxml"));
            Parent root = loader.load();
            usernameLabel.getScene().setRoot(root);
        } catch (IOException e) {
            messageLabel.setText("Could not load login screen.");
        }
    }
}
