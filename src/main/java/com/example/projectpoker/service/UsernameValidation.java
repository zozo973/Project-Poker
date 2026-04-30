package com.example.projectpoker.service;

import com.example.projectpoker.controller.RegisterController;

public class UsernameValidation {
    // check if username is blank
    public static ValidationResult checkUsernameBlank(String username){
        if (username.isBlank()) {
            return ValidationResult.fail("Username can't be empty", "clearUsername");
        }
        else {
            return ValidationResult.ok();
        }
    }
    // check if username contains 3 or more characters
    public static ValidationResult checkUsernameLength(String username) {
        if (username.length() < 3) {
            return ValidationResult.fail("Username must be 3 or more characters.", "clearUsername");
        }
        else {
            return ValidationResult.ok();
        }
    }
    // check if username contains no illegal characters
    public static ValidationResult checkUsernameIllegal(String username) {
        boolean hasIllegalUsername = RegisterController.ILLEGAL_CHARS.matcher(username).find();
        if (hasIllegalUsername) {
            return ValidationResult.fail("Username must contain no illegal characters/spaces.", "clearUsername");
        }
        else {
            return ValidationResult.ok();
        }
    }
}
