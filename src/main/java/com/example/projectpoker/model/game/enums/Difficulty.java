package com.example.projectpoker.model.game.enums;


public enum Difficulty {
    BABY("Like playing with a bunch of infant children."),
    WHAT_IS_POKER("Someone who has never played the game before."),
    GAMBLING_ADDICT("Has taken another mortgage out on their house to play"),
    PROFESSIONAL("You will be robbed."),
    SADISTIC("You will never financially, physically or emotionally recover.");

    private final String description;

    Difficulty(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}