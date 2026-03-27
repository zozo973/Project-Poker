package com.example.projectpoker.model;

public class User {
    private int id;
    private String username;
    private String password;
    private int totalHandsPlayed;
    private int totalWins;

    public User(int id, String username, String password, int totalHandsPlayed, int totalWins) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.totalHandsPlayed = totalHandsPlayed;
        this.totalWins = totalWins;
    }

    public User(String username, String password, int totalHandsPlayed, int totalWins) {
        this.username = username;
        this.password = password;
        this.totalHandsPlayed = totalHandsPlayed;
        this.totalWins = totalWins;
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
}