package com.example.projectpoker.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.example.projectpoker.service.PasswordHasher;

class RegisterControllerTest {
    // to test password hashing
    @Test
    void hashing_ShouldVerifyCorrectly() {
        String raw = "SecurePass123!";
        String hash = PasswordHasher.hash(raw);

        assertTrue(PasswordHasher.verify(raw, hash));
        assertNotEquals(raw, hash);
    }
}