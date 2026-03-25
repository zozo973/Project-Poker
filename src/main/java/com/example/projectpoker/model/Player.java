package com.example.projectpoker.model;

import java.util.ArrayList;

import static com.example.projectpoker.model.statistics.SkewNormalSampler.safeRoundToInt;

public class Player {
    private ArrayList<Card> playerHand;
    private String name;
    private boolean isTurn;
    private int balance;
    private int roundInvestment;
    private Action action;
    private Roles role;

    // No args constructor, minimum balance a player starts a
    // game with if they do not choose to use money they have won before.
    public Player() {
        this.name = "";
        this.playerHand = new ArrayList<>();
        this.isTurn = false;
        this.action = null;
        this.role = Roles.PLAYER;
        this.roundInvestment = 0;
    }

    public Player(String name) {
        this.name = name;
        this.playerHand = new ArrayList<>();
        this.isTurn = false;
        this.action = null;
        this.role = Roles.PLAYER;
        this.roundInvestment = 0;
    }

    public Player(String name, int balance) {
        this.name = name;
        this.playerHand = new ArrayList<>();
        this.isTurn = false;
        this.balance = balance;
        this.action = null;
        this.role = Roles.PLAYER;
        this.roundInvestment = 0;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public void addCardToHand(Card c) { this.playerHand.add(c); }

    public ArrayList<Card> getPlayerHand() { return playerHand; }

    public int getBalance() { return balance; }

    protected void setBalance(int balance) { this.balance = balance; }

    public int getRoundInvestment() { return roundInvestment; }

    protected void setRoundInvestment(int roundInvestment) { this.roundInvestment = roundInvestment; }

    public void setAction(Action action) { this.action = action; }

    public Action getAction() { return action; }

    public boolean getIsTurn() { return isTurn; }

    public void setIsTurn(boolean isTurn) { this.isTurn = isTurn; }

    public Roles getRole() { return role; }

    public void setRole(Roles role) { this.role = role; }

    public int placeBet(int betSize) {
        if (betSize >= balance) {
            // TODO add method that queries if player wants to go all in
            // If player accepts to all in then
            setBalance(0);
            // TODO implement method that lets user know they don't have that much
            // If a slider is used to get user input for betting size this should be redundant
            System.err.println("User betting more then there balance");

            System.out.println("Would you like to go all in?");
            // wait for user response
            boolean response = false; // allIn();
            if (response) {
                betSize = balance;
                // calculate the main pot this player can win
            } else {
                // if responds is to fold
                this.action = Action.FOLD;
                betSize = -1;
            }
        } else {
            setBalance((balance - betSize));
        }
        roundInvestment += betSize;
        return betSize;
    }

    public int payBlind(int blindSize) {
        int blind = safeRoundToInt(role.getBlindMultiplier() * blindSize);
        return placeBet(blind);
    }

    public int chooseBetSize() {
        // return rounded slider value from the view
        return 0;
    }
}
