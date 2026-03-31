package com.example.projectpoker.model.game;

import java.util.ArrayList;

public class Pot {
    private ArrayList<Player> players;
    private int potSize;

    public Pot(ArrayList<Player> players) {
        this.players = players;
        this.potSize = 0;
    }

    public int getPotSize() { return potSize; }

    public void setPotSize(int potSize) { this.potSize = potSize; }

    public void addToPot(int bet) { this.potSize += bet; }
}
