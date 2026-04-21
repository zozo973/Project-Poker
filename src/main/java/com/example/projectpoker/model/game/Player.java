package com.example.projectpoker.model.game;

import com.example.projectpoker.model.Hand;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Roles;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;

import static com.example.projectpoker.model.statistics.SkewNormalSampler.safeRoundToInt;

public class Player {
    // Player Events
    //      balance Change
    //      Role Change
    //      isTurn Change
    //      Action Change

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Hand playerHand;
    private PlayerId id;
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
        this.id = new PlayerId();
        this.playerHand = new Hand();
        this.isTurn = false;
        this.action = Action.UNDECIDED;
        this.balance = 1000;
        this.role = Roles.PLAYER;
        this.roundInvestment = 0;
    }

    public Player(String name) {
        this.name = name;
        this.id = new PlayerId();
        this.playerHand = new Hand();
        this.isTurn = false;
        this.action = Action.UNDECIDED;
        this.balance = 1000;
        this.role = Roles.PLAYER;
        this.roundInvestment = 0;
    }

    public Player(String name, Roles role) {
        this.name = name;
        this.id = new PlayerId();
        this.playerHand = new Hand();
        this.isTurn = false;
        this.balance = 1000;
        this.action = Action.UNDECIDED;
        this.role = role;
        this.roundInvestment = 0;
    }

    public Player(String name, int balance) {
        this.name = name;
        this.id = new PlayerId();
        this.playerHand = new Hand();
        this.isTurn = false;
        this.balance = balance;
        this.action = Action.UNDECIDED;
        this.role = Roles.PLAYER;
        this.roundInvestment = 0;
    }

    public Player(String name, int balance, String id) throws IOException {
        this.name = name;
        this.id = new PlayerId(id);
        this.playerHand = new Hand();
        this.isTurn = false;
        this.balance = balance;
        this.action = Action.UNDECIDED;
        this.role = Roles.PLAYER;
        this.roundInvestment = 0;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public PlayerId getId() { return id; }

    public void setId(PlayerId id) { this.id = id; }

    public boolean matchId(PlayerId id) {
        if (this.id.getId().compareTo(id.getId()) == 0) {
            return true;
        }
        return false;
    }

    public Hand getPlayerHand() { return playerHand; }

    public void addCardToHand(Card c) {
        this.playerHand.addCard(c);
    }

    // TODO implement validation for UI input
    public int getBalance() { return balance; }

    public void subtractBalance(int amount) { setBalance(this.balance - amount); }

    protected void setBalance(int balance) {
        // Fire a change balance event
        var oldVal = this.balance;
        this.balance = balance;
        pcs.firePropertyChange("balance",oldVal,this.balance);
    }

    public int getRoundInvestment() { return roundInvestment; }

    protected void setRoundInvestment(int roundInvestment) { this.roundInvestment = roundInvestment; }

    public Action getAction() { return action; }

    public void setAction(Action action) {
        // Fire a change Action event
        var oldVal = this.action;
        this.action = action;
        pcs.firePropertyChange("action",oldVal,this.action);
    }

    public boolean getIsTurn() { return isTurn; }

    public void setIsTurn(boolean isTurn) {
        // Fire a change isTurn event
        var oldVal = this.isTurn;
        this.isTurn = isTurn;
        pcs.firePropertyChange("isTurn",oldVal,this.isTurn);
    }

    public Roles getRole() { return role; }

    public void setRole(Roles role) {
        // Fire a change Role event
        var oldVal = this.role;
        this.role = role;
        pcs.firePropertyChange("role",oldVal,this.role);
    }

    public void roundReset() {
        pcs.firePropertyChange("roundReset",this, new Player(getName(),getBalance()));
        this.playerHand.clear();
        this.isTurn = false;
        this.action = null;
        this.role = Roles.PLAYER;
        this.roundInvestment = 0;
    }

    public void win(int potSize) {
        this.balance += potSize;
        //
    }

    public int placeBet(int betSize) {
        int b = getBalance();

        if (betSize <= 0) {
            throw new IllegalArgumentException("Bet must be positive.");
        }
        if (betSize > b) {

            // TODO implement method that lets user know they don't have that much
            // If a slider is used to get user input for betting size this should be redundant

            throw new IllegalArgumentException("Bet exceeds player balance.");

        } else if (betSize == b) {
            // TODO add method that queries if player wants to go all in
            // If player accepts to all in then
            // throw new IllegalArgumentException("Bet exceeds player balance.");
            System.out.println("Would you like to go all in?");
            // wait for user response
            boolean response = false; // allIn();
            setBalance(0);
            if (response) {
                betSize = b;
                allIn();
                // calculate the main pot this player can win
            } else {
                // if responds is to fold
                this.action = Action.FOLD;
                betSize = -1;
            }
        } else {
            setBalance((b - betSize));
        }
        this.roundInvestment += betSize;
        return betSize;
    }

    public int payBlind(int blindSize) {
        if (this.role == Roles.PLAYER || this.role == Roles.DEALER) return 0;
        int blind = safeRoundToInt(role.getBlindMultiplier() * blindSize);
        return placeBet(blind);
    }

    public void allIn() {
        int betAmount = this.balance;
        if (betAmount > 0) {
            subtractBalance(betAmount);
        }
    }

    public int chooseBetSize() {
        // return rounded slider value from the view

        return 0;
    }

    public void forfeitGame() {
        // Send current balance to database and exit game.
        if (this.action.equals(Action.FOLD)) {
            setAction(Action.FORFEIT);
        } else {
            System.out.println("Do you wish to forfeit all invested money in the round and exit?");

        }

    }


}
