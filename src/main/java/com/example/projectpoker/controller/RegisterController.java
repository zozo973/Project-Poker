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
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    // for making sure usernames and passwords don't contain illegal characters
    private static final Pattern ILLEGAL_CHARS =
            Pattern.compile("[\\s\\\\\"'<>\\u0000-\\u001F;:,/| \\[\\]]");
    // for making sure emails don't contain illegal characters
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Common illegal characters for usernames and passwords (from google search)
    //      Spaces: Often forbidden, especially in usernames, as they can break systems that parse input.
    //      Backs lash (\): Frequently disallowed in passwords because it acts as an escape character.
    //      Quotes (" and '): Double and single quotes are often blocked to prevent SQL injection attacks.
    //          Angle Brackets (<, >): Often restricted to prevent HTML/scripting injection (XSS).
    //      Control Characters: Any non-printable characters (ASCII 0-31).
    //      Other Special Characters: Semicolon (;), colon (:), comma (,), slash (/), pipe (|), and brackets ([, ])

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // if statements to check if a user has inputted something correctly in the fields

        //  check if the user has inputted something in the fields
        if ( username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ) {
            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
            messageLabel.setText("Invalid Username, Email or Password! Please fill all fields.");
            return;
        }
        // check if password and confirm password match
        if (!password.equals(confirmPassword)) {
            passwordField.setText("");
            confirmPasswordField.setText("");
            messageLabel.setText("Please make sure passwords match.");
            return;
        }
        // check if username contains 3 or more characters
        if (username.length() < 3) {
            usernameField.setText("");
            messageLabel.setText("Username must be 3 or more characters.");
            return;
        }
        // variable for checking illegal characters
        boolean hasIllegalUsername = ILLEGAL_CHARS.matcher(username).find();
        // check if username contains no illegal characters
        if (hasIllegalUsername) {
            usernameField.setText("");
            messageLabel.setText("Username must contain no illegal characters/spaces.");
            return;
        }

        // check if email meets criteria
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailField.setText("");
            messageLabel.setText("Please enter a valid email address.");
            return;
        }

        // check if password is correct length of 8 characters
        if ( password.length() < 8 ){
            passwordField.setText("");
            confirmPasswordField.setText("");
            messageLabel.setText("Please make sure password contains 8 or more characters.");
            return;
        }
        // variable for checking lack of a number or special character
        boolean isWeakPassword = !password.matches(".*[0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        // check if password contains numbers or special characters
        if (isWeakPassword){
            passwordField.setText("");
            confirmPasswordField.setText("");
            messageLabel.setText("Please make sure password contains a number or special character.");
            return;
        }
        // variable for checking illegal characters
        boolean hasIllegalPassword = ILLEGAL_CHARS.matcher(password).find();
        // check if password contains illegal characters
        if ( hasIllegalPassword ){
            passwordField.setText("");
            confirmPasswordField.setText("");
            messageLabel.setText("Please make sure password contains 8 or more characters.");
            return;
        }

        // hashing password then verifying password was hashed currently
        String hashedPassword = PasswordHasher.hash(password);
        boolean verifiedPassword = PasswordHasher.verify(password, hashedPassword);
        if (!verifiedPassword) {
            passwordField.setText("");
            confirmPasswordField.setText("");
            messageLabel.setText("Something went wrong, please try again.");
            return;
        }

        // create and save user
        User newUser = new User(username, hashedPassword, email);
        UserDAO userDAO = new UserDAO();
        if (userDAO.getByUsername(username) != null) {
            messageLabel.setText("Username already taken!");
            return;
        }
        userDAO.insert(newUser);

        // set user as logged in
        SessionManager.setCurrentUser(newUser);
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
