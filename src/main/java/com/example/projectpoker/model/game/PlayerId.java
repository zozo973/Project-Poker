package com.example.projectpoker.model.game;

import java.io.IOException;
import java.security.SecureRandom;

public class PlayerId {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final int length = 12;
    private String id;

    /** No args constructor
     *      Used when generating a player object without a unique identifier.
     */

    public PlayerId() {
        this.id = generateRandomId();
    }

    /** Main constructor
     *      Used if a player object already has a unique identifier.
     * @param id: players unique identifier
     * @throws IOException: Input Output exception thrown from validateId if id is invalid.
     */

    public PlayerId(String id) throws IOException {
        validateId(id);
    }

    public String getId() { return id; }

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

    public static String generateRandomId() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

}
