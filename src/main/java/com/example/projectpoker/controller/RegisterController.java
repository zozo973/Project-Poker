package com.example.projectpoker.controller;

import com.example.projectpoker.PokerApplication;
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

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // variables to check if the user has inputted something in the fields
        boolean checkedUsername = username.isBlank();
        boolean checkedEmail = email.isBlank();
        boolean checkedPassword = password.isBlank();
        boolean checkedConfirmPassword = confirmPassword.isBlank();

        // if statement to check if a user has inputted something correctly in the fields
        if (checkedUsername || checkedEmail || checkedPassword || checkedConfirmPassword) {
            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
            messageLabel.setText("Invalid Username, Email or Password! Please fill all fields.");
            return;
        }
        // check if password and confirm password match
        else if (!password.equals(confirmPassword)) {
            passwordField.setText("");
            confirmPasswordField.setText("");
            messageLabel.setText("Please makes sure passwords match.");
            return;
        }
        // hashing password then verifying password was hashed currently
        else {
            String hashedPassword = PasswordHasher.hash(password);
            boolean verifiedPassword = PasswordHasher.verify(password, hashedPassword);

            if (!verifiedPassword) {
                passwordField.setText("");
                confirmPasswordField.setText("");
                messageLabel.setText("Something went wrong, please try again.");
                return;
            }

            // creating a new User object using registration constructor
            User newUser = new User(username, hashedPassword, email);
            // create UserDAO object then call insert method
            UserDAO userDAO = new UserDAO();
            userDAO.createTable();
            userDAO.insert(newUser);
            }

        // look up the user
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getByUsername(username);
        // set user as logged in
        SessionManager.setCurrentUser(user);

        try {
            PokerApplication app = new PokerApplication();
            app.createPokerGame();
        } catch (IOException e) {
            messageLabel.setText("Could not start game: " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/Account & Profile UI/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Could not load login screen.");
        }
    }
}
