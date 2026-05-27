package com.example.projectpoker.service;

import com.example.projectpoker.controller.RegisterController;

public class PasswordValidation {

    /**
     * Checks that neither the password nor the confirm-password field is blank (used for registration).
     *
     * @param password        the password field value
     * @param confirmPassword the confirm-password field value
     * @return {@link ValidationResult#ok()} if both are non-blank; a failure result otherwise
     */
    public static ValidationResult checkBothPasswordBlank(String password, String confirmPassword) {
        if (password.isBlank() || confirmPassword.isBlank()) {
            return ValidationResult.fail("Passwords can't be empty.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }
    /**
     * Checks that the password field is not blank (used for login).
     *
     * @param password the password field value
     * @return {@link ValidationResult#ok()} if non-blank; a failure result otherwise
     */
    public static ValidationResult checkPasswordBlank(String password) {
        if (password.isBlank()) {
            return ValidationResult.fail("Password can't be empty.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }
    /**
     * Checks that the password and the confirm-password fields match.
     *
     * @param password        the password field value
     * @param confirmPassword the confirm-password field value
     * @return {@link ValidationResult#ok()} if they match; a failure result otherwise
     */
    public static ValidationResult checkPasswordConfirm(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return ValidationResult.fail("Please make sure passwords match.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }
    /**
     * Checks that the password is at least 8 characters long.
     *
     * @param password the password string to check
     * @return {@link ValidationResult#ok()} if length &ge; 8; a failure result otherwise
     */
    public static ValidationResult checkPasswordLength(String password) {
        if ( password.length() < 8 ){
            return ValidationResult.fail("Please make sure password contains 8 or more characters.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }

    /**
     * Checks that the password contains at least one digit or special character
     * (prevents trivially weak passwords).
     *
     * @param password the password string to check
     * @return {@link ValidationResult#ok()} if at least one strong character is found; a failure result otherwise
     */
    public static ValidationResult checkWeakPassword(String password) {
        boolean isWeakPassword = !RegisterController.WEAK_CHARS.matcher(password).find();
        if (isWeakPassword){
            return ValidationResult.fail("Please make sure password contains a number or special character.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }

    /**
     * Checks that the password contains no illegal characters.
     *
     * @param password the password string to check
     * @return {@link ValidationResult#ok()} if no illegal characters are present; a failure result otherwise
     */
    public static ValidationResult checkIllegalPassword(String password) {
        boolean hasIllegalPassword = RegisterController.ILLEGAL_CHARS.matcher(password).find();
        if (hasIllegalPassword) {
            return ValidationResult.fail("Make sure password doesn't contain illegal characters.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }
}
