package com.example.projectpoker.model.game;

import java.io.IOException;
import java.security.SecureRandom;

public class PlayerId {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final int length = 12;
    private String id;

    /**
     * Creates a new player identifier with a randomly generated 12-character alphanumeric ID.
     */
    public PlayerId() {
        this.id = generateRandomId();
    }

    /**
     * Creates a player identifier with a specific ID string.
     * The ID must be exactly 12 characters long and contain only alphanumeric characters (A–Z, a–z, 0–9).
     *
     * @param id the 12-character alphanumeric id string
     * @throws IOException if the id is invalid length or contains invalid characters
     */

    public PlayerId(String id) throws IOException {
        validateId(id);
    }

    /**
     * Returns the string identifier value for this player.
     *
     * @return the 12-character alphanumeric id string
     */
    public String getId() { return id; }

    /**
     * Overrides the id value directly (should only be used where validation has already occurred).
     *
     * @param id the new id string to assign
     */
    public void setId(String id) { this.id = id; }

    private void validateId(String id) throws IOException {
        if (id.length() != length) {
            throw new IOException("Id is invalid length");
        }
        for (char ch : id.toCharArray()) {
            if (!CHARACTERS.contains(String.valueOf(ch))) {
                throw new IOException("Id is invalid, it contains invalid characters");
            }
        }
        this.id = id;
    }

    /**
     * Generates a new cryptographically random 12-character alphanumeric string.
     *
     * @return a random id string of length 12 using characters A–Z, a–z, and 0–9
     */
    public static String generateRandomId() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

}
