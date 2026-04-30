package com.example.projectpoker.controller;

import com.example.projectpoker.PokerApplication;
import com.example.projectpoker.model.User;
import com.example.projectpoker.database.UserDAO;
import com.example.projectpoker.service.SessionManager;
import com.example.projectpoker.service.UsernameValidation;
import com.example.projectpoker.service.EmailValidation;
import com.example.projectpoker.service.PasswordValidation;
import com.example.projectpoker.service.ValidationResult;
import com.example.projectpoker.service.PasswordHasher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.util.List;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    // Common illegal characters for usernames and passwords (from google search)
    //      Spaces: Often forbidden, especially in usernames, as they can break systems that parse input.
    //      Backs lash (\): Frequently disallowed in passwords because it acts as an escape character.
    //      Quotes (" and '): Double and single quotes are often blocked to prevent SQL injection attacks.
    //          Angle Brackets (<, >): Often restricted to prevent HTML/scripting injection (XSS).
    //      Control Characters: Any non-printable characters (ASCII 0-31).
    //      Other Special Characters: Semicolon (;), colon (:), comma (,), slash (/), pipe (|), and brackets ([, ])

    public static final Pattern WEAK_CHARS = Pattern.compile(".*[0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    public static final Pattern ILLEGAL_CHARS = Pattern.compile("[\\s\\\\\"'<>\\u0000-\\u001F;:,/| \\[\\]]");
    public static final Pattern EMAIL_CHARS = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        List<ValidationResult> checkedRegisterResults = List.of(
                UsernameValidation.checkUsernameBlank(username),
                UsernameValidation.checkUsernameLength(username),
                UsernameValidation.checkUsernameIllegal(username),
                EmailValidation.checkEmailBlank(email),
                EmailValidation.checkIllegalEmail(email),
                PasswordValidation.checkBothPasswordBlank(password, confirmPassword),
                PasswordValidation.checkPasswordConfirm(password, confirmPassword),
                PasswordValidation.checkPasswordLength(password),
                PasswordValidation.checkIllegalPassword(password),
                PasswordValidation.checkWeakPassword(password)
        );

        ValidationResult failure = null;
        for (ValidationResult result : checkedRegisterResults) {
            if (!result.isValid()) {
                failure = result;
                break;
            }
        }
        if (failure != null) {
            switch (failure.getFieldToClear()) {
                case "clearUsername" -> usernameField.setText("");
                case "clearPassword" -> { passwordField.setText(""); confirmPasswordField.setText(""); }
                case "clearEmail"    -> emailField.setText("");
            }
            messageLabel.setText(failure.getMessage());
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
            usernameField.setText("");
            return;
        }
        userDAO.insert(newUser);

        // set user as logged in
//        SessionManager.setCurrentUser(newUser);
//        try {
//            PokerApplication app = new PokerApplication();
//            app.createPokerGame();
//        } catch (IOException e) {
//            messageLabel.setText("Could not start game: " + e.getMessage());
//        }
        SessionManager.setCurrentUser(newUser);
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            PokerApplication app = new PokerApplication();
            app.createPokerGame(stage);
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
