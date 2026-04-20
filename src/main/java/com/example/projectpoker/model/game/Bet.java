package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;

import java.util.ArrayList;

public class Bet {
    private int betSize;
    private Pot pot;

    public Bet() {
        this.betSize = 0;
    }

    public Bet(Pot pot) {
        this.betSize = 0;
        this.pot = pot;
    }

    public Bet(int betSize, Pot pot) {
        this.betSize = betSize;
        this.pot = pot;
    }

    public int getBetSize() { return betSize; }

    public void setBetSize(int betSize) { this.betSize = betSize; }

    public Pot getPot() { return pot; }

    public void setPot(Pot pot) { this.pot = pot; }

}
