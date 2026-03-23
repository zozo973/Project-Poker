package com.example.projectpoker.GameLogic;

public class Player {
    private Hand hand = new Hand();
    private String name;

    public Player(String name) {
        this.name = name;
    }

    public Hand getHand() {
        return hand;
    }

    public String getName() {
        return name;
    }
}