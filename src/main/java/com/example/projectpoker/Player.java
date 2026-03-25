package com.example.projectpoker;

import com.example.projectpoker.model.Action;
import com.example.projectpoker.model.Card;
import com.example.projectpoker.model.Roles;

public class Player {
    private Card[] playerHand;
    private boolean isTurn;
    private int balance;
    private Action action;
    private Roles role;

    // No args constructor, minimum balance a player starts a
    // game with if they do not choose to use money they have won before.
    public Player() {
        this.playerHand = new Card[5];
        this.isTurn = false;
        this.action = null;
        this.role = Roles.PLAYER;
    }

    public Player(int balance) {
        this.playerHand = new Card[5];
        this.isTurn = false;
        this.balance = balance;
        this.action = null;
        this.role = Roles.PLAYER;
    }

    public Card[] getPlayerHand() { return playerHand; }

    public int getBalance() { return balance; }

    protected void setBalance(int balance) { this.balance = balance; }

    public void setAction(Action action) { this.action = action; }

    public Action getAction() { return action; }

    public boolean getIsTurn() { return isTurn; }

    public void setIsTurn(boolean isTurn) { this.isTurn = isTurn; }

    public Roles getRole() { return role; }

    public void setRole(Roles role) { this.role = role; }

    public void placeBet(int betSize) {
        if (betSize == balance) {
            // TODO add method that queries if player wants to go all in
        } else if (betSize > balance) {
            // TODO implement method that lets user know they don't have that much
            // If a slider is used to get user input for betting size this should be redundant
            System.err.println("User betting more then there balance");
        } else {
            setBalance((balance - betSize));
        }
    }
}
