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
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.totalHandsPlayed = 0;
        this.totalWins = 0;
        this.currentBalance = 1000;
    }

    // constructor for loading existing user from database
     public User(int id, String username, String password, String email, int totalHandsPlayed, int totalWins, int currentBalance) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.totalHandsPlayed = totalHandsPlayed;
        this.totalWins = totalWins;
        this.currentBalance = currentBalance;
    }

    // getters
    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public int getTotalHandsPlayed() {
        return totalHandsPlayed;
    }
    public int getTotalWins() {
        return totalWins;
    }
    public int getCurrentBalance() {
        return currentBalance;
    }

    // setters
    public void setId(int id) {
        this.id = id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setTotalHandsPlayed(int totalHandsPlayed) {
        this.totalHandsPlayed = totalHandsPlayed;
    }
    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }
    public void setCurrentBalance(int currentBalance) {
        this.currentBalance = currentBalance;
    }
}