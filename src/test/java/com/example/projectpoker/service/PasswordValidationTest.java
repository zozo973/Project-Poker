package com.example.projectpoker.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordValidationTest {

    // checkBothPasswordBlank() tests

    @Test
    void checkBothPasswordBlank_failsWhenPasswordBlank() {
        ValidationResult result = PasswordValidation.checkBothPasswordBlank("", "TestPass1!");
        assertFalse(result.isValid(), "Should fail when password is blank");
        assertEquals("clearPassword", result.getFieldToClear());
    }

    @Test
    void checkBothPasswordBlank_failsWhenConfirmPasswordBlank() {
        ValidationResult result = PasswordValidation.checkBothPasswordBlank("TestPass1!", "");
        assertFalse(result.isValid(), "Should fail when confirm password is blank");
    }

    @Test
    void checkBothPasswordBlank_failsWhenBothBlank() {
        ValidationResult result = PasswordValidation.checkBothPasswordBlank("", "");
        assertFalse(result.isValid(), "Should fail when both passwords are blank");
    }

    @Test
    void checkBothPasswordBlank_passesWhenBothFilled() {
        ValidationResult result = PasswordValidation.checkBothPasswordBlank("TestPass1!", "TestPass1!");
        assertTrue(result.isValid(), "Should pass when both passwords are filled");
    }

    // checkPasswordBlank() tests

    @Test
    void checkPasswordBlank_failsWhenBlank() {
        ValidationResult result = PasswordValidation.checkPasswordBlank("");
        assertFalse(result.isValid(), "Should fail when password is blank");
        assertEquals("clearPassword", result.getFieldToClear());
    }

    @Test
    void checkPasswordBlank_passesWhenNotBlank() {
        ValidationResult result = PasswordValidation.checkPasswordBlank("TestPass1!");
        assertTrue(result.isValid(), "Should pass when password is not blank");
    }

    // checkPasswordConfirm() tests

    @Test
    void checkPasswordConfirm_failsWhenPasswordsDontMatch() {
        ValidationResult result = PasswordValidation.checkPasswordConfirm("TestPass1!", "DifferentPass1!");
        assertFalse(result.isValid(), "Should fail when passwords don't match");
        assertEquals("clearPassword", result.getFieldToClear());
    }

    @Test
    void checkPasswordConfirm_passesWhenPasswordsMatch() {
        ValidationResult result = PasswordValidation.checkPasswordConfirm("TestPass1!", "TestPass1!");
        assertTrue(result.isValid(), "Should pass when passwords match");
    }

    // checkPasswordLength() tests

    @Test
    void checkPasswordLength_failsWhenTooShort() {
        ValidationResult result = PasswordValidation.checkPasswordLength("abc");
        assertFalse(result.isValid(), "Should fail when password is less than 8 characters");
        assertEquals("clearPassword", result.getFieldToClear());
    }

    @Test
    void checkPasswordLength_failsWhenExactly7Characters() {
        ValidationResult result = PasswordValidation.checkPasswordLength("1234567");
        assertFalse(result.isValid(), "Should fail when password is exactly 7 characters");
    }

    @Test
    void checkPasswordLength_passesWhenExactly8Characters() {
        ValidationResult result = PasswordValidation.checkPasswordLength("12345678");
        assertTrue(result.isValid(), "Should pass when password is exactly 8 characters");
    }

    @Test
    void checkPasswordLength_passesWhenLongerThan8Characters() {
        ValidationResult result = PasswordValidation.checkPasswordLength("TestPass1!");
        assertTrue(result.isValid(), "Should pass when password is longer than 8 characters");
    }

    // checkWeakPassword() tests

    @Test
    void checkWeakPassword_failsWhenNoNumbersOrSpecialChars() {
        ValidationResult result = PasswordValidation.checkWeakPassword("testpassword");
        assertFalse(result.isValid(), "Should fail when password has no numbers or special characters");
        assertEquals("clearPassword", result.getFieldToClear());
    }

    @Test
    void checkWeakPassword_passesWhenContainsNumber() {
        ValidationResult result = PasswordValidation.checkWeakPassword("testpassword1");
        assertTrue(result.isValid(), "Should pass when password contains a number");
    }

    @Test
    void checkWeakPassword_passesWhenContainsSpecialChar() {
        ValidationResult result = PasswordValidation.checkWeakPassword("testpassword!");
        assertTrue(result.isValid(), "Should pass when password contains a special character");
    }

    // checkIllegalPassword() tests

    @Test
    void checkIllegalPassword_failsWhenContainsSpace() {
        ValidationResult result = PasswordValidation.checkIllegalPassword("test pass1!");
        assertFalse(result.isValid(), "Should fail when password contains a space");
        assertEquals("clearPassword", result.getFieldToClear());
    }

    @Test
    void checkIllegalPassword_failsWhenContainsSemicolon() {
        ValidationResult result = PasswordValidation.checkIllegalPassword("testpass;1!");
        assertFalse(result.isValid(), "Should fail when password contains a semicolon");
    }

    @Test
    void checkIllegalPassword_passesWithValidPassword() {
        ValidationResult result = PasswordValidation.checkIllegalPassword("TestPass1!");
        assertTrue(result.isValid(), "Should pass with a valid password");
    }
}