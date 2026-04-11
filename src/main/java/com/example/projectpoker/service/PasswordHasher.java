package com.example.projectpoker.service;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHasher {
    // this controls how long the hashing takes
    private static final int COST = 12;

    // hash password
    public static String hash(String password) {
        return BCrypt.withDefaults()
                .hashToString(COST, password.toCharArray());
    }

    // verify password
    public static boolean verify(String password, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer()
                .verify(password.toCharArray(), hashedPassword);

        return result.verified;
    }
}