package com.example.projectpoker.model.game;

import java.util.ArrayList;

public class RoundInvestment {
    private int totalInvestment;
    private ArrayList<Bet> bets;

    /** No args Constructor
     *      Creates an empty RoundInvestment object.
     */

    public RoundInvestment() {
        this.totalInvestment = 0;
        this.bets = new ArrayList<>();
    }

    /** Constructor
     *      Creates a RoundInvestment object without setting any bets
     * @param totalInvestment: total amount of money invested in a round.
     */

    public RoundInvestment(int totalInvestment) {
        this.totalInvestment = totalInvestment;
        this.bets = new ArrayList<>();
    }


    /**
     * Returns the full list of individual bets made across all pots this round.
     *
     * @return list of {@link Bet} objects in the order they were placed
     */
    public ArrayList<Bet> getBets() {
        return bets;
    }

    /**
     * Returns only the bets that were placed into the specified pot.
     *
     * @param pot the {@link Pot} to filter by
     * @return list of {@link Bet} objects whose pot reference equals {@code pot}
     */
    public ArrayList<Bet> getBetsByPot(Pot pot) {
        ArrayList<Bet> betsIntoPot = new ArrayList<>();
        for (Bet b  : this.bets) {
            if (b.getPot().equals(pot)) betsIntoPot.add(b);
        }
        return betsIntoPot;
    }

    /**
     * Returns the most recently added bet in this investment record.
     *
     * @return the last {@link Bet} in the list
     */
    public Bet getLastBet() { return bets.getLast(); }

    /**
     * Replaces the entire list of bets with the given list.
     *
     * @param bets the new list of {@link Bet} objects to use
     */
    public void setBets(ArrayList<Bet> bets) {
        this.bets = bets;
    }

    /**
     * Appends a new bet to this investment record and increments the total investment.
     *
     * @param betSize the chip amount of the new bet
     * @param pot     the {@link Pot} the bet was placed into
     */
    public void add2Bets(int betSize, Pot pot) {
        this.bets.add(new Bet(betSize, pot));
        add2TotalInvestment(betSize);
    }

    /**
     * Returns the total chips this player has invested across all pots this round.
     *
     * @return cumulative chip investment for the round
     */
    public int getTotalInvestment() {
        return totalInvestment;
    }

    /**
     * Directly sets the total investment value.
     *
     * @param totalInvestment the new total investment amount
     */
    public void setTotalInvestment(int totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    private void add2TotalInvestment(int val) { this.totalInvestment += val; }

    /**
     * Splits the bet record upon side-pot creation so that each investment is correctly
     * attributed to the right pot at the right priority level.
     *
     * @param pot the newly created or adjusted {@link Pot} whose priority and investment
     *            ceiling are used to re-map existing bets
     */
    public void reInit(Pot pot) {
        ArrayList<Bet> adjustBets = new ArrayList<>();
        for (Bet bet : this.bets) {
            if (bet.getPot().getPotPriority()>pot.getPotPriority()) {
                // if SidePot has lower priority then bets pot field, adjust bet and pot.
                Bet adjustedBet = new Bet(
                        bet.getBetSize() - pot.getInvestmentPP(),
                        bet.getPot()
                );
                adjustBets.add(adjustedBet);
            } else {
                adjustBets.add(bet);
            }
        }
        adjustBets.add(new Bet(pot.getInvestmentPP(),pot));
        this.bets = adjustBets;
    }

    /**
     * Resets this investment record to zero bets, as required at the start of each new round.
     */
    public void reset() {
        this.totalInvestment = 0;
        this.bets = new ArrayList<>();
    }
}
