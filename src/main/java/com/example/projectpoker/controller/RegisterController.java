package com.example.projectpoker.controller;

import com.example.projectpoker.service.PasswordHasher;
import com.example.projectpoker.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    // unsure whether should be private or public
    private void handleRegister() {
        String username = usernameField.getText();
        String email =  emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // variables to check if the user has inputted something in the fields
        boolean checkedUsername = username.isBlank();
        boolean checkedEmail = email.isBlank();
        boolean checkedPassword = password.isBlank();
        boolean checkedConfirmPassword = confirmPassword.isBlank();

        // i see a potential problem being that once the if statement finishes it continues to run methods under it maybe a while loop might be smart i have no idea

        // if statement to check if a user has inputted something correctly in the fields
        if (checkedUsername || checkedEmail || checkedPassword || checkedConfirmPassword) {
            // display something like please invalid entry please fill all fields
            // make sure nothing changes or username, email and password fields are reset so it's empty for the user to retype them out
            usernameField.setText(null);
            emailField.setText(null);
            passwordField.setText(null);
            confirmPasswordField.setText(null);

            messageLabel.setText("Invalid Username, Email or Password! Please fill all fields.");

        }
        // check if password and confirm password match
        else if (!password.equals(confirmPassword)) {
            usernameField.setText(null);
            emailField.setText(null);
            passwordField.setText(null);
            confirmPasswordField.setText(null);

            messageLabel.setText("Please, make sure both passwords are the same.");
        }

        // hashing password then verifying password was hashed currently
        // if not makes user input everything again
        // this is probably not a good way to handle it because use has no idea what the issue is
        // and it's probably not there fault

        String hashedPassword = PasswordHasher.hash(password);
        boolean verifiedPassword = PasswordHasher.verify(password, hashedPassword);

        if (!verifiedPassword) {
            usernameField.setText(null);
            emailField.setText(null);
            passwordField.setText(null);
            confirmPasswordField.setText(null);

            messageLabel.setText("Password doesn't match after hashing!");
        }

        // creating a new User object using registration constructor
        User newUser = new User(username, email, hashedPassword);

        // create UserDAO object then call method
        // I have no idea why i cant call anything in UserDAO. It's a public class with public methods
        UserDAO UserObj = new UserDAO();
        insert(User newUser);

        // success message
        messageLabel.setText("Successful Registration!");

    }

    // i think it should be public but i have no idea
    public void goToLogin() {

    }
}
