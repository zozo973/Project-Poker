package com.example.projectpoker.model.game;

public enum Roles {
    PLAYER(0,"Player"),
    DEALER(0,"Dealer"),
    BIGBLIND(1, "Big Blind"),
    SMALLBLIND(0.5, "Small blind");

    private final double blindMultiplier;
    private final String description;

    Roles(double blindMultiplier, String description) {
        this.blindMultiplier = blindMultiplier;
        this.description = description;
    }

    public double getBlindMultiplier() { return this.blindMultiplier; }
}
