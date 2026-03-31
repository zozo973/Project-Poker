package com.example.projectpoker.model.game;

public enum Difficulty {
    BABY("Like playing with a bunch of infant children"),
    WHATISPOKER("Someone who has never played the game before"),
    GAMBLINGADDICT("Has taken another mortgage out on there house to play"),
    PROFESSIONAL("You will be robbed"),
    WSOPTABLE("You will never financially, physically and emotionally recover");

    private final String desciption;

    Difficulty(String desciption) {this.desciption = desciption; }

}
