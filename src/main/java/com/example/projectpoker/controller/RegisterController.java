package com.example.projectpoker.controller;

import com.example.projectpoker.database.UserDAO;
import com.example.projectpoker.model.User;
import com.example.projectpoker.service.PasswordHasher;
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

    private void handleRegister() {
        String username = usernameField.getText();
        String email =  emailField.getText();
        String password = passwordField.getText();

        boolean checkedUsername = username.isBlank();
        boolean checkedEmail = email.isBlank();
        boolean checkedPassword = password.isBlank();

        if (checkedUsername || checkedEmail || checkedPassword) {
            // display something like please invalid entry please fill all fields
            // make sure nothing changes or username, email and password fields are reset so it's empty for the user to retype them out
        }



    }

    private void goToLogin() {
        
    }
}
