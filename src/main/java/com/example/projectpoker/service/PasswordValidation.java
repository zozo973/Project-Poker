package com.example.projectpoker.service;

import com.example.projectpoker.controller.RegisterController;

public class PasswordValidation {
    // check if both passwords is blank for Register
    public static ValidationResult checkBothPasswordBlank(String password, String confirmPassword) {
        if (password.isBlank() || confirmPassword.isBlank()) {
            return ValidationResult.fail("Passwords can't be empty.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }
    // check if password for login is blank
    public static ValidationResult checkPasswordBlank(String password) {
        if (password.isBlank()) {
            return ValidationResult.fail("Password can't be empty.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }
    // check if password and confirm password match
    public static ValidationResult checkPasswordConfirm(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return ValidationResult.fail("Please make sure passwords match.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }
    // check if password is correct length of 8 characters
    public static ValidationResult checkPasswordLength(String password) {
        if ( password.length() < 8 ){
            return ValidationResult.fail("Please make sure password contains 8 or more characters.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }

    // check if password contains numbers or special characters
    public static ValidationResult checkWeakPassword(String password) {
        boolean isWeakPassword = !RegisterController.WEAK_CHARS.matcher(password).find();
        if (isWeakPassword){
            return ValidationResult.fail("Please make sure password contains a number or special character.", "clearPassword");
        }
        else {
            return ValidationResult.ok();
        }
    }

    // check if password contains illegal characters
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



