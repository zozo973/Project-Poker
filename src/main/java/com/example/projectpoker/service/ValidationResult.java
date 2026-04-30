package com.example.projectpoker.service;

// container about knowing if user input has failed and what message to show
public class ValidationResult {
    private final boolean isValid;
    private final String message;
    private final String fieldToClear;

    public ValidationResult (boolean valid, String message, String fieldToClear) {
        this.isValid = valid;
        this.message = message;
        this.fieldToClear = fieldToClear;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, null, null);
    }

    public static ValidationResult fail(String message, String fieldToClear) {
        return new ValidationResult(false, message, fieldToClear);
    }

    public boolean isValid() { return isValid; }
    public String getMessage() { return message; }
    public String getFieldToClear() { return fieldToClear; }
}