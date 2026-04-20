package com.example.projectpoker.model.game;

import java.util.ArrayList;

public class RoundInvestment {
    private int totalInvestment;
    private ArrayList<Bet> bets;

    public RoundInvestment() {
        this.totalInvestment = 0;
        this.bets = new ArrayList<>();
    }

    public RoundInvestment(int totalInvestment) {
        this.totalInvestment = totalInvestment;
        this.bets = new ArrayList<>();
    }

    public RoundInvestment(int totalInvestment, ArrayList<Bet> bets) {
        this.totalInvestment = totalInvestment;
        this.bets = bets;
    }

    public ArrayList<Bet> getBets() {
        return bets;
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

    public void reset() {
        this.totalInvestment = 0;
        this.bets = new ArrayList<>();
    }
}
