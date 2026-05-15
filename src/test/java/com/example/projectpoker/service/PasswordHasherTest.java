package com.example.projectpoker.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    // hash() tests

    @Test
    void hash_returnsNonNull() {
        // Arrange
        String password = "TestPassword1!";
        // Act
        String hashed = PasswordHasher.hash(password);
        // Assert
        assertNotNull(hashed, "Hashed password should not be null");
    }

    @Test
    void hash_returnsDifferentStringFromOriginal() {
        // Arrange
        String password = "TestPassword1!";
        // Act
        String hashed = PasswordHasher.hash(password);
        // Assert
        assertNotEquals(password, hashed, "Hashed password should not equal the original");
    }

    @Test
    void hash_twoDifferentHashesForSamePassword() {
        String password = "TestPassword1!";
        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);
        assertNotEquals(hash1, hash2, "Two hashes of the same password should differ due to random salt");
    }

    // verify() tests

    @Test
    void verify_returnsTrueForCorrectPassword() {
        // Arrange
        String password = "TestPassword1!";
        String hashed = PasswordHasher.hash(password);
        // Act
        boolean result = PasswordHasher.verify(password, hashed);
        // Assert
        assertTrue(result, "verify() should return true for the correct password");
    }

    @Test
    void verify_returnsFalseForWrongPassword() {
        // Arrange
        String password = "TestPassword1!";
        String wrongPassword = "WrongPassword9@";
        String hashed = PasswordHasher.hash(password);
        // Act
        boolean result = PasswordHasher.verify(wrongPassword, hashed);
        // Assert
        assertFalse(result, "verify() should return false for an incorrect password");
    }

    @Test
    void verify_returnsFalseForEmptyPassword() {
        // Arrange
        String password = "TestPassword1!";
        String hashed = PasswordHasher.hash(password);
        // Act
        boolean result = PasswordHasher.verify("", hashed);
        // Assert
        assertFalse(result, "verify() should return false for an empty password");
    }
}