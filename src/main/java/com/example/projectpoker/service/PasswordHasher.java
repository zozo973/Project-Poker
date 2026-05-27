package com.example.projectpoker.service;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHasher {
    // this controls how long the hashing takes
    private static final int COST = 12;

    /**
     * Hashes the given plain-text password using BCrypt.
     *
     * @param password the plain-text password to hash
     * @return a BCrypt hash string suitable for database storage
     */
    public static String hash(String password) {
        return BCrypt.withDefaults()
                .hashToString(COST, password.toCharArray());
    }

    /**
     * Verifies that a plain-text password matches a stored BCrypt hash.
     *
     * @param password       the plain-text password entered by the user
     * @param hashedPassword the BCrypt hash retrieved from the database
     * @return {@code true} if the password matches the hash, {@code false} otherwise
     */
    public static boolean verify(String password, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer()
                .verify(password.toCharArray(), hashedPassword);

        return result.verified;
    }
}