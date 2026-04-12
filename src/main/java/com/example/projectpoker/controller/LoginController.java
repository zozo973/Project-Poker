package com.example.projectpoker.controller;

import com.example.projectpoker.service.PasswordHasher;
import com.example.projectpoker.model.User;
import com.example.projectpoker.database.UserDAO;
import com.example.projectpoker.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
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

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // variables to check if the user has inputted something in the fields
        boolean checkedUsername = username.isBlank();
        boolean checkedPassword = password.isBlank();

        // if statement to check if a user has inputted something correctly in the fields
        if (checkedUsername || checkedPassword) {
            usernameField.setText("");
            passwordField.setText("");
            messageLabel.setText("Invalid Username or Password! Please fill all fields.");
            return;
        }

        else {
            // look up the user
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getByUsername(username);

            // check if username is correct
            if (user == null) {
                messageLabel.setText("Username not found.");
                return;
            }

            boolean verifiedPassword = PasswordHasher.verify(password, user.getPassword());

            // check if password is correct
            if (!verifiedPassword) {
                messageLabel.setText("Incorrect password.");
                return;
            }

            SessionManager.setCurrentUser(user);
            messageLabel.setText("Login successful!");
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
}
