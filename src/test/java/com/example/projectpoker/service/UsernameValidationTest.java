package com.example.projectpoker.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsernameValidationTest {

    // checkUsernameBlank() tests

    @Test
    void checkUsernameBlank_failsWhenBlank() {
        ValidationResult result = UsernameValidation.checkUsernameBlank("");
        assertFalse(result.isValid(), "Should fail when username is blank");
        assertEquals("clearUsername", result.getFieldToClear());
        assertNotNull(result.getMessage(), "Should return an error message");
    }

    @Test
    void checkUsernameBlank_failsWhenOnlySpaces() {
        ValidationResult result = UsernameValidation.checkUsernameBlank("   ");
        assertFalse(result.isValid(), "Should fail when username is only spaces");
    }

    @Test
    void checkUsernameBlank_passesWhenNotBlank() {
        ValidationResult result = UsernameValidation.checkUsernameBlank("basil");
        assertTrue(result.isValid(), "Should pass when username is not blank");
    }

    // --- checkUsernameLength() tests ---

    @Test
    void checkUsernameLength_failsWhenTooShort() {
        ValidationResult result = UsernameValidation.checkUsernameLength("ab");
        assertFalse(result.isValid(), "Should fail when username is less than 3 characters");
        assertEquals("clearUsername", result.getFieldToClear());
    }

    @Test
    void checkUsernameLength_failsWhenSingleCharacter() {
        ValidationResult result = UsernameValidation.checkUsernameLength("a");
        assertFalse(result.isValid(), "Should fail when username is a single character");
    }

    @Test
    void checkUsernameLength_passesWhenExactly3Characters() {
        ValidationResult result = UsernameValidation.checkUsernameLength("abc");
        assertTrue(result.isValid(), "Should pass when username is exactly 3 characters");
    }

    @Test
    void checkUsernameLength_passesWhenLongerThan3Characters() {
        ValidationResult result = UsernameValidation.checkUsernameLength("basil");
        assertTrue(result.isValid(), "Should pass when username is longer than 3 characters");
    }

    // checkUsernameIllegal() tests

    @Test
    void checkUsernameIllegal_failsWhenContainsSpace() {
        ValidationResult result = UsernameValidation.checkUsernameIllegal("bas il");
        assertFalse(result.isValid(), "Should fail when username contains a space");
        assertEquals("clearUsername", result.getFieldToClear());
    }

    @Test
    void checkUsernameIllegal_failsWhenContainsSemicolon() {
        ValidationResult result = UsernameValidation.checkUsernameIllegal("bas;il");
        assertFalse(result.isValid(), "Should fail when username contains a semicolon");
    }

    @Test
    void checkUsernameIllegal_failsWhenContainsBackslash() {
        ValidationResult result = UsernameValidation.checkUsernameIllegal("bas\\il");
        assertFalse(result.isValid(), "Should fail when username contains a backslash");
    }

    @Test
    void checkUsernameIllegal_failsWhenContainsSingleQuote() {
        ValidationResult result = UsernameValidation.checkUsernameIllegal("bas'il");
        assertFalse(result.isValid(), "Should fail when username contains a single quote");
    }

    @Test
    void checkUsernameIllegal_passesWithValidUsername() {
        ValidationResult result = UsernameValidation.checkUsernameIllegal("basil97");
        assertTrue(result.isValid(), "Should pass with a valid username");
    }

    @Test
    void checkUsernameIllegal_passesWithUnderscoreAndNumbers() {
        ValidationResult result = UsernameValidation.checkUsernameIllegal("basil_97");
        assertTrue(result.isValid(), "Should pass with underscores and numbers");
    }
}