package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;

import java.util.ArrayList;

public class Bet {
    private int betSize;
    private Pot pot;

    /** Main Constructor
     *      used to make a bet such that a players bets can be tracked and counted over multiple pots.
     * @param betSize: integer amount quantifying the bet.
     * @param pot: The Pot object the bet was made into.
     */

    public Bet(int betSize, Pot pot) {
        this.betSize = betSize;
        this.pot = pot;
    }

    public int getBetSize() { return betSize; }

    public void setBetSize(int betSize) { this.betSize = betSize; }

    public Pot getPot() { return pot; }

    public void setPot(Pot pot) { this.pot = pot; }

}
