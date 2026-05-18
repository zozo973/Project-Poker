package com.example.projectpoker.model.game.enums;


public enum Difficulty {
    Baby("Like playing with a bunch of infant children."),
    Beginner("Someone who has never played the game before."),
    Addict("Has taken another mortgage out on their house to play"),
    Professional("You will be robbed."),
    Sadist("You will never financially, physically or emotionally recover.");

    private final String description;

    Difficulty(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}