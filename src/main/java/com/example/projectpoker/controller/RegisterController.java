package com.example.projectpoker.controller;

import com.example.projectpoker.database.UserDAO;
import com.example.projectpoker.model.User;
import com.example.projectpoker.service.PasswordHasher;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;

    private void handleRegister() {



    }

    private void goToLogin() {

    }
}
