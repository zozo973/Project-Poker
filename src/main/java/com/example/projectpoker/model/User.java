package com.example.projectpoker.model;

import java.time.LocalDateTime;

public class User {
    // fields that go into the User Database table
    private int id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;
    private int totalHandsPlayed;
    private int totalWins;
    private int currentBalance;

    // constructor for registration - stats default to 0, balance defaults to 1000
    /**
     * Creates a new in-memory user for registration with default stats and starting balance.
     *
     * @param username chosen account username
     * @param password hashed password string
     * @param email account email address
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.totalHandsPlayed = 0;
        this.totalWins = 0;
        this.currentBalance = 1000;
    }

    // constructor for loading existing user from database
    /**
     * Creates a user object populated from persisted database values.
     *
     * @param id user primary key
     * @param username account username
     * @param password hashed password string
     * @param email account email address
     * @param totalHandsPlayed number of hands played across sessions
     * @param totalWins number of game-session wins
     * @param currentBalance current chip balance
     */
    public User(int id, String username, String password, String email, int totalHandsPlayed, int totalWins, int currentBalance) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.totalHandsPlayed = totalHandsPlayed;
        this.totalWins = totalWins;
        this.currentBalance = currentBalance;
    }

    /**
     * Returns the database id of this user.
     *
     * @return the user's integer primary key from the users table
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the user's login username.
     *
     * @return the username string
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the user's hashed password.
     *
     * @return the BCrypt hashed password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user's email address.
     *
     * @return the email string, or {@code null} if not set
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the timestamp when this user account was created.
     *
     * @return the account creation date-time, or {@code null} if not yet persisted
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the total number of poker hands this user has completed.
     *
     * @return the hands played count
     */
    public int getTotalHandsPlayed() {
        return totalHandsPlayed;
    }

    /**
     * Returns the total number of game sessions this user has won.
     *
     * @return the win count
     */
    public int getTotalWins() {
        return totalWins;
    }

    /**
     * Returns this user's current chip balance as stored in the database.
     *
     * @return the current balance in chips
     */
    public int getCurrentBalance() {
        return currentBalance;
    }

    /**
     * Sets the database id for this user.
     *
     * @param id the new primary key value
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the user's login username.
     *
     * @param username the new username to assign
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the user's password (should already be hashed via {@link com.example.projectpoker.service.PasswordHasher}).
     *
     * @param password the BCrypt hashed password string
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the new email string
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the account creation timestamp.
     *
     * @param createdAt the date-time to record as the creation time
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Sets the total number of hands played by this user.
     *
     * @param totalHandsPlayed the updated hands-played count
     */
    public void setTotalHandsPlayed(int totalHandsPlayed) {
        this.totalHandsPlayed = totalHandsPlayed;
    }

    /**
     * Sets the total number of game-session wins for this user.
     *
     * @param totalWins the updated win count
     */
    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    /**
     * Sets the user's current chip balance.
     *
     * @param currentBalance the new balance value to persist
     */
    public void setCurrentBalance(int currentBalance) {
        this.currentBalance = currentBalance;
    }
}