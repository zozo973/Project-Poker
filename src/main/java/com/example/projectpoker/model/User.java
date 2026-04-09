package com.example.projectpoker.model;

public class User {
    private int id;
    private String username;
    private String password;
    private int totalHandsPlayed;
    private int totalWins;
    private int currentBalance;

    public User(int id, String username, String password, int totalHandsPlayed, int totalWins, int currentBalance) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.totalHandsPlayed = totalHandsPlayed;
        this.totalWins = totalWins;
        this.currentBalance = currentBalance;
    }

    public User(String username, String password, int totalHandsPlayed, int totalWins) {
        this(username, password, totalHandsPlayed, totalWins, 1000);
    }

    public User(String username, String password, int totalHandsPlayed, int totalWins, int currentBalance) {
        this.username = username;
        this.password = password;
        this.totalHandsPlayed = totalHandsPlayed;
        this.totalWins = totalWins;
        this.currentBalance = currentBalance;
    }

    public User(int id, String username, String password, int totalHandsPlayed, int totalWins) {
        this(id, username, password, totalHandsPlayed, totalWins, 1000);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getTotalHandsPlayed() {
        return totalHandsPlayed;
    }

    public void setTotalHandsPlayed(int totalHandsPlayed) {
        this.totalHandsPlayed = totalHandsPlayed;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public int getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(int currentBalance) {
        this.currentBalance = currentBalance;
    }
}
