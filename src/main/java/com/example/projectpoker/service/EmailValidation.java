package com.example.projectpoker.service;

import com.example.projectpoker.controller.RegisterController;

public class EmailValidation {

    /**
     * Checks that the email field is not blank.
     *
     * @param email the raw email string from the input field
     * @return {@link ValidationResult#ok()} if non-blank; a failure result otherwise
     */
    public static ValidationResult checkEmailBlank(String email){
        if (email.isBlank()) {
            return ValidationResult.fail("Email can't be empty", "clearEmail");
        }
        else {
            return ValidationResult.ok();
        }
    }

    /**
     * Checks that the email address matches the standard email format
     * ({@code local@domain.tld}).
     *
     * @param email the raw email string to validate
     * @return {@link ValidationResult#ok()} if the format is valid; a failure result otherwise
     */
    public static ValidationResult checkIllegalEmail(String email) {

        boolean hasIllegalEmail = !RegisterController.EMAIL_CHARS.matcher(email).find();
        if (hasIllegalEmail) {
            return ValidationResult.fail("Please enter a valid email address.", "clearEmail");
        }
        else {
            return ValidationResult.ok();
        }
    }
}
