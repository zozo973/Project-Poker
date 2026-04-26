package com.example.projectpoker.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.example.projectpoker.service.PasswordHasher;

class RegisterControllerTest {

    // to test email validation
    @Test
    void emailValidation_ShouldAcceptValidEmail() {
        String validEmail = "test@example.com";
        assertTrue(RegisterController.EMAIL_PATTERN.matcher(validEmail).matches());
    }
    // to test username validation
    @Test
    void usernameValidation_ShouldRejectSpaces() {
        String invalidUsername = "Poker Player";
        assertTrue(RegisterController.ILLEGAL_CHARS.matcher(invalidUsername).find());
    }
    // to test password hashing
    @Test
    void hashing_ShouldVerifyCorrectly() {
        String raw = "SecurePass123!";
        String hash = PasswordHasher.hash(raw);

        assertTrue(PasswordHasher.verify(raw, hash));
        assertNotEquals(raw, hash);
    }
}