package com.example.projectpoker.controller;

import com.example.projectpoker.model.User;
import com.example.projectpoker.service.PasswordHasher;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private final LoginController controller = new LoginController();

    @Test
    void shouldReturnErrorIfFieldsAreBlank() {
        String result = controller.getLoginValidationMessage(null, "", "password123");
        assertEquals("Invalid Username or Password! Please fill all fields.", result);
    }

    @Test
    void shouldReturnErrorIfUserNotFound() {
        // User is null because UserDAO couldn't find them
        String result = controller.getLoginValidationMessage(null, "fakeUser", "password123");
        assertEquals("Username not found.", result);
    }

    @Test
    void shouldReturnErrorIfPasswordIsWrong() {
        // Arrange: Create a user with a hashed password
        String correctPass = "realPassword";
        String hashedPass = PasswordHasher.hash(correctPass);
        User mockUser = new User("testUser", hashedPass, "test@test.com");

        // Act: Try logging in with the wrong password
        String result = controller.getLoginValidationMessage(mockUser, "testUser", "wrongPassword");

        // Assert
        assertEquals("Incorrect password.", result);
    }

    @Test
    void shouldReturnSuccessForCorrectCredentials() {
        String pass = "pokerPlayer1";
        String hashed = PasswordHasher.hash(pass);
        User mockUser = new User("pokerPlayer1", hashed, "poker@test.com");

        String result = controller.getLoginValidationMessage(mockUser, "pokerPlayer1", pass);
        assertEquals("SUCCESS", result);
    }
}