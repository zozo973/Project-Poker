package com.example.projectpoker.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidationTest {

    // checkEmailBlank() tests

    @Test
    void checkEmailBlank_failsWhenBlank() {
        ValidationResult result = EmailValidation.checkEmailBlank("");
        assertFalse(result.isValid(), "Should fail when email is blank");
        assertEquals("clearEmail", result.getFieldToClear());
        assertNotNull(result.getMessage(), "Should return an error message");
    }

    @Test
    void checkEmailBlank_failsWhenOnlySpaces() {
        ValidationResult result = EmailValidation.checkEmailBlank("   ");
        assertFalse(result.isValid(), "Should fail when email is only spaces");
    }

    @Test
    void checkEmailBlank_passesWhenNotBlank() {
        ValidationResult result = EmailValidation.checkEmailBlank("test@email.com");
        assertTrue(result.isValid(), "Should pass when email is not blank");
    }

    // checkIllegalEmail() tests

    @Test
    void checkIllegalEmail_failsWhenNoAtSymbol() {
        ValidationResult result = EmailValidation.checkIllegalEmail("notanemail");
        assertFalse(result.isValid(), "Should fail when email has no @ symbol");
        assertEquals("clearEmail", result.getFieldToClear());
    }

    @Test
    void checkIllegalEmail_failsWhenNoDomain() {
        ValidationResult result = EmailValidation.checkIllegalEmail("test@");
        assertFalse(result.isValid(), "Should fail when email has no domain after @");
    }

    @Test
    void checkIllegalEmail_failsWhenNoLocalPart() {
        ValidationResult result = EmailValidation.checkIllegalEmail("@email.com");
        assertFalse(result.isValid(), "Should fail when email has nothing before @");
    }

    @Test
    void checkIllegalEmail_failsWhenNoDotInDomain() {
        ValidationResult result = EmailValidation.checkIllegalEmail("test@emailcom");
        assertFalse(result.isValid(), "Should fail when domain has no dot");
    }

    @Test
    void checkIllegalEmail_failsWhenContainsSpaces() {
        ValidationResult result = EmailValidation.checkIllegalEmail("test @email.com");
        assertFalse(result.isValid(), "Should fail when email contains spaces");
    }

    @Test
    void checkIllegalEmail_passesWithValidEmail() {
        ValidationResult result = EmailValidation.checkIllegalEmail("test@email.com");
        assertTrue(result.isValid(), "Should pass with a valid email address");
    }

    @Test
    void checkIllegalEmail_passesWithSubdomain() {
        ValidationResult result = EmailValidation.checkIllegalEmail("test@mail.email.com");
        assertTrue(result.isValid(), "Should pass with a subdomain email address");
    }

    @Test
    void checkIllegalEmail_passesWithPlusSign() {
        ValidationResult result = EmailValidation.checkIllegalEmail("test+filter@email.com");
        assertTrue(result.isValid(), "Should pass with a plus sign in local part");
    }

    @Test
    void checkIllegalEmail_passesWithUnderscoreAndDot() {
        ValidationResult result = EmailValidation.checkIllegalEmail("test.user_name@email.com");
        assertTrue(result.isValid(), "Should pass with underscores and dots in local part");
    }
}