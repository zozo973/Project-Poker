package com.example.projectpoker.service;

import com.example.projectpoker.controller.RegisterController;

public class UsernameValidation {

    /**
     * Checks that the username is not blank.
     *
     * @param username the raw username string from the input field
     * @return {@link ValidationResult#ok()} if non-blank; a failure result otherwise
     */
    public static ValidationResult checkUsernameBlank(String username){
        if (username.isBlank()) {
            return ValidationResult.fail("Username can't be empty", "clearUsername");
        }
        else {
            return ValidationResult.ok();
        }
    }

    /**
     * Checks that the username contains at least 3 characters.
     *
     * @param username the raw username string
     * @return {@link ValidationResult#ok()} if length &ge; 3; a failure result otherwise
     */
    public static ValidationResult checkUsernameLength(String username) {
        if (username.length() < 3) {
            return ValidationResult.fail("Username must be 3 or more characters.", "clearUsername");
        }
        else {
            return ValidationResult.ok();
        }
    }

    /**
     * Checks that the username contains no illegal characters (spaces, special symbols, etc.).
     *
     * @param username the raw username string
     * @return {@link ValidationResult#ok()} if no illegal characters are found; a failure result otherwise
     */
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
