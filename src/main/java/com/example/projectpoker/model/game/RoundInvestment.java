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


    public ArrayList<Bet> getBets() {
        return bets;
    }

    public ArrayList<Bet> getBetsByPot(Pot pot) {
        ArrayList<Bet> betsIntoPot = new ArrayList<>();
        for (Bet b  : this.bets) {
            if (b.getPot().equals(pot)) betsIntoPot.add(b);
        }
        return betsIntoPot;
    }

    public Bet getLastBet() { return bets.getLast(); }

    public void setBets(ArrayList<Bet> bets) {
        this.bets = bets;
    }

    public void add2Bets(int betSize, Pot pot) {
        this.bets.add(new Bet(betSize, pot));
        add2TotalInvestment(betSize);
    }

    public int getTotalInvestment() {
        return totalInvestment;
    }

    public void setTotalInvestment(int totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    private void add2TotalInvestment(int val) { this.totalInvestment += val; }

    /** reInit
     * @param pot: Pot object to be reInitialised.
     *
     *      Method reInitialises the pot param according to a users bets in the event of a sidePot being created.
     *
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

    public void reset() {
        this.totalInvestment = 0;
        this.bets = new ArrayList<>();
    }
}
