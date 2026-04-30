package com.example.projectpoker.service;

import com.example.projectpoker.controller.RegisterController;

public class EmailValidation {
    // check if email is blank
    public static ValidationResult checkEmailBlank(String email){
        if (email.isBlank()) {
            return ValidationResult.fail("Email can't be empty", "clearEmail");
        }
        else {
            return ValidationResult.ok();
        }
    }
    // check if email meets criteria
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
