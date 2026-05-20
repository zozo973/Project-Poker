package com.example.projectpoker.controller;

import com.example.projectpoker.service.*;
import com.example.projectpoker.model.User;
import com.example.projectpoker.database.UserDAO;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        List<ValidationResult> checkedLoginResults = List.of(
                UsernameValidation.checkUsernameBlank(username),
                PasswordValidation.checkPasswordBlank(password)
        );

        ValidationResult failure = null;
        for (ValidationResult result : checkedLoginResults) {
            if (!result.isValid()) {
                failure = result;
                break;
            }
        }
        if (failure != null) {
            switch (failure.getFieldToClear()) {
                case "clearUsername" -> usernameField.setText("");
                case "clearPassword" -> passwordField.setText("");
            }
            messageLabel.setText(failure.getMessage());
        }
        else {
            // look up the user
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getByUsername(username);

            // check if username is correct
            if (user == null) {
                messageLabel.setText("Username not found.");
                usernameField.setText("");
                return;
            }

            boolean verifiedPassword = PasswordHasher.verify(password, user.getPassword());
            // check if password is correct
            if (!verifiedPassword) {
                messageLabel.setText("Incorrect password.");
                passwordField.setText("");
                return;
            }

            SessionManager.setCurrentUser(user);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/projectpoker/MainMenu.fxml"));
                Parent root = loader.load();
                usernameField.getScene().setRoot(root);
            } catch (IOException e) {
                messageLabel.setText("Could not load Main Menu.");
            }
        }
    }


    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/Account & Profile UI/register.fxml"));
            Parent root = loader.load();
            usernameField.getScene().setRoot(root);
        } catch (IOException e) {
            messageLabel.setText("Could not load register screen.");
        }
    }
}
