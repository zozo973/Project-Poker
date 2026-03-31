package com.example.projectpoker.model.game;

public enum RoundStatus {

    BLINDS("Set the blinds"),
    DEAL("Deal the cards"),
    BETTING1("First round of betting"),
    FLOP("Flop"),
    BETTING2("Second round of betting"),
    TURN("Turn"),
    BETTING3("Third round of betting"),
    RIVER("River"),
    SHOWDOWN("Showdown"),
    END("Game over");

    private final String description;

    RoundStatus (String description) {
        this.description = description;
    }
}
