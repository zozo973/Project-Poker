package com.example.projectpoker.model;

public enum Stage {

    PREFLOP("Pre-Flop"),
    FLOP("Flop"),
    TURN("Turn"),
    RIVER("River"),
    SHOWDOWN("Showdown");

    private final String description;

    Stage(String description) {
        this.description = description;
    }
}
