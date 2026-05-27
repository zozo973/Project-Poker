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

    /**
     * Returns the chip amount of this bet.
     *
     * @return the bet size in chips
     */
    public int getBetSize() { return betSize; }

    /**
     * Sets the chip amount for this bet.
     *
     * @param betSize the new bet size in chips
     */
    public void setBetSize(int betSize) { this.betSize = betSize; }

    /**
     * Returns the pot this bet was placed into.
     *
     * @return the associated {@link Pot}
     */
    public Pot getPot() { return pot; }

    /**
     * Sets the pot associated with this bet.
     *
     * @param pot the {@link Pot} to associate with this bet
     */
    public void setPot(Pot pot) { this.pot = pot; }

}
