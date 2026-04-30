package com.example.projectpoker.controller;

import com.example.projectpoker.service.*;
import com.example.projectpoker.model.User;
import com.example.projectpoker.database.UserDAO;
import com.example.projectpoker.PokerApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.util.List;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    // TODO: Fix tight coupling with handlelogin() doing everything
    //      if im using test cases, the database connection shouldn't be triggering, etc.
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
            return;
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
                Stage stage = (Stage) usernameField.getScene().getWindow();
                PokerApplication app = new PokerApplication();
                app.createPokerGame(stage);
            } catch (IOException e) {
                messageLabel.setText("Could not start game: " + e.getMessage());
            }
        }
    }

    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/Account & Profile UI/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Could not load register screen.");
        }
    }
    // temp method that is used for unit tests
    String getLoginValidationMessage(User user, String username, String password) {
        if (username.isBlank() || password.isBlank()) {
            return "Invalid Username or Password! Please fill all fields.";
        }
        if (user == null) {
            return "Username not found.";
        }
        boolean verifiedPassword = PasswordHasher.verify(password, user.getPassword());
        if (!verifiedPassword) {
            return "Incorrect password.";
        }
        return "SUCCESS";
    }
}
