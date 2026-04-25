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
    private final Hand playerHand;
    private PlayerId id;
    private String name;
    private boolean isTurn;
    private int balance;
    private RoundInvestment roundInvestment;
    private Action action;
    private Roles role;
    private Integer activeBet;

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
        this.roundInvestment = new RoundInvestment();
        this.activeBet = null;
    }

    public Player(String name) {
        this.name = name;
        this.id = new PlayerId();
        this.playerHand = new Hand();
        this.isTurn = false;
        this.action = Action.UNDECIDED;
        this.balance = 1000;
        this.role = Roles.PLAYER;
        this.roundInvestment = new RoundInvestment();
        this.activeBet = null;
    }

    public Player(String name, Roles role) {
        this.name = name;
        this.id = new PlayerId();
        this.playerHand = new Hand();
        this.isTurn = false;
        this.balance = 1000;
        this.action = Action.UNDECIDED;
        this.role = role;
        this.roundInvestment = new RoundInvestment();
        this.activeBet = null;
    }

    public Player(String name, int balance) {
        this.name = name;
        this.id = new PlayerId();
        this.playerHand = new Hand();
        this.isTurn = false;
        this.balance = balance;
        this.action = Action.UNDECIDED;
        this.role = Roles.PLAYER;
        this.roundInvestment = new RoundInvestment();
        this.activeBet = null;
    }

    public Player(String name, int balance, String id) throws IOException {
        this.name = name;
        this.id = new PlayerId(id);
        this.playerHand = new Hand();
        this.isTurn = false;
        this.balance = balance;
        this.action = Action.UNDECIDED;
        this.role = Roles.PLAYER;
        this.roundInvestment = new RoundInvestment();
        this.activeBet = null;
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
        return this.id.getId().compareTo(id.getId()) == 0;
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

    public RoundInvestment getRoundInvestment() { return roundInvestment; }

    public int getTotalInvestment() { return roundInvestment.getTotalInvestment(); }

    public int getTotalPotInvestment(Pot pot) {
        ArrayList<Bet> betsInPot = roundInvestment.getBetsByPot(pot);
        int potInvestment = 0;
        for (Bet b : betsInPot) {
            potInvestment += b.getBetSize();
        }
        return potInvestment;
    }

    protected void setRoundInvestment(int totalInvested) { this.roundInvestment = new RoundInvestment(totalInvested); }

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

    public Integer getActiveBet() { return activeBet; }

    public void setActiveBet(Integer activeBet) { this.activeBet = activeBet; }

    public void roundReset() {
        pcs.firePropertyChange("roundReset",this, new Player(getName(),getBalance()));
        this.playerHand.clear();
        this.isTurn = false;
        this.action = Action.UNDECIDED;
        this.roundInvestment.reset();
        this.activeBet = null;
    }

    public void win(int potSize) {
        this.balance += potSize;
    }

    public int placeBet(int betSize, Pot pot) {
        int b = getBalance();

        if (betSize <= 0) {
            throw new IllegalArgumentException("Bet must be positive.");
        }

        else if (betSize == b) {
            setBalance(0);
            if (this.action != Action.ALLIN) {
                this.action = Action.ALLIN;
            }
        } else {
            setBalance((b - betSize));
        }
        this.activeBet = betSize;
        this.roundInvestment.add2Bets(betSize,pot);
        return betSize;
    }

    public int payBlind(int blindSize, Pot pot) {
        if (this.role == Roles.PLAYER || this.role == Roles.DEALER) return 0;

        int blind = safeRoundToInt(role.getBlindMultiplier() * blindSize);
        int balance = getBalance();
        if (balance <= 0) {
            return 0;
        }

        int blindToPay = Math.min(blind, balance);
        return placeBet(blindToPay,pot);
    }

    public void allIn() {
        int betAmount = this.balance;
        if (betAmount > 0) {
            subtractBalance(betAmount);
        }
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
