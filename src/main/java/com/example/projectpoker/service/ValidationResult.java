package com.example.projectpoker.service;

// container about knowing if user input has failed and what message to show
public class ValidationResult {
    private final boolean isValid;
    private final String message;
    private final String fieldToClear;

    /**
     * Creates an immutable validation result.
     *
     * @param valid whether the validated input passed all checks
     * @param message message describing the validation error, or {@code null} when valid
     * @param fieldToClear identifier of the UI field to clear on failure, or {@code null}
     */
    public ValidationResult (boolean valid, String message, String fieldToClear) {
        this.isValid = valid;
        this.message = message;
        this.fieldToClear = fieldToClear;
    }

    /**
     * Creates a successful validation result with no error message or field to clear.
     *
     * @return a {@link ValidationResult} indicating success
     */
    public static ValidationResult ok() {
        return new ValidationResult(true, null, null);
    }

    /**
     * Creates a failed validation result with a user-facing error message.
     *
     * @param message       the error message to display to the user
     * @param fieldToClear  the identifier of the UI field that should be cleared
     *                      (e.g. {@code "clearUsername"}, {@code "clearPassword"}, {@code "clearEmail"})
     * @return a {@link ValidationResult} indicating failure
     */
    public static ValidationResult fail(String message, String fieldToClear) {
        return new ValidationResult(false, message, fieldToClear);
    }

    /**
     * Returns whether the validation passed.
     *
     * @return {@code true} if valid, {@code false} if the input failed validation
     */
    public boolean isValid() { return isValid; }

    /**
     * Returns the error message associated with this validation failure, or {@code null} if the result is valid.
     *
     * @return the error message string, or {@code null}
     */
    public String getMessage() { return message; }

    /**
     * Returns the identifier of the UI field that should be cleared when this validation fails.
     *
     * @return the field-to-clear string (e.g. {@code "clearPassword"}), or {@code null} if the result is valid
     */
    public String getFieldToClear() { return fieldToClear; }
}