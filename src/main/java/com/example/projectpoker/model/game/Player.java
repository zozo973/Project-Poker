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
    private int minBet;
    private RoundInvestment roundInvestment;
    private Action action;
    private Roles role;
    private Integer activeBet;

    /** No args constructor
     *      Minimum balance a player starts a game with if they do not choose to use money they have won before.
     *      Used in testing the Player class and in AiPlayer to set the main fields.
     */

    public Player() {
        this.name = "";
        this.id = new PlayerId();
        this.playerHand = new Hand();
        this.isTurn = false;
        this.action = Action.UNDECIDED;
        this.balance = 1000;
        this.minBet = 15;
        this.role = Roles.PLAYER;
        this.roundInvestment = new RoundInvestment();
        this.activeBet = null;
    }

    /** Constructor called for testing other classes that use player objects
     *
     * @param name: Player's name, either users name from database or AiPlayer name.
     * @param balance: Players starting balance
     * @param id: Players unique identifier used to match hand evaluation winner to a player object
     * @param blindSize: blindSize represents the smallest bet a player can make
     */

    public Player(String name, int balance, String id, int blindSize) throws IOException {
        this.name = name;
        this.id = new PlayerId(id);
        this.playerHand = new Hand();
        this.isTurn = false;
        this.balance = balance;
        this.minBet = blindSize;
        this.action = Action.UNDECIDED;
        this.role = Roles.PLAYER;
        this.roundInvestment = new RoundInvestment();
        this.activeBet = null;
    }

    /** Main constructor, used to instantiate a player outside of testing.
     *
     * @param name: Player's name, either users name from database or AiPlayer name.
     * @param balance: Players starting balance
     */

    public Player(String name, int balance) {
        this.name = name;
        this.id = new PlayerId();
        this.playerHand = new Hand();
        this.isTurn = false;
        this.balance = balance;
        this.minBet = 15;
        this.action = Action.UNDECIDED;
        this.role = Roles.PLAYER;
        this.roundInvestment = new RoundInvestment();
        this.activeBet = null;
    }

    /**
     * Registers a listener that is notified whenever any property of this player changes.
     *
     * @param listener the {@link PropertyChangeListener} to register
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Registers a listener that is notified whenever a specific named property changes.
     *
     * @param propertyName the name of the property to observe (e.g. "balance", "action", "isTurn", "role")
     * @param listener     the {@link PropertyChangeListener} to register
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a previously registered listener from all property notifications.
     *
     * @param listener the {@link PropertyChangeListener} to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Removes a previously registered listener from notifications for a specific property.
     *
     * @param propertyName the name of the property whose listener should be removed
     * @param listener     the {@link PropertyChangeListener} to remove
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Returns the display name of this player.
     *
     * @return the player's name string
     */
    public String getName() { return name; }

    /**
     * Sets the display name of this player.
     *
     * @param name the new name to assign
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the unique identifier for this player.
     *
     * @return the player's {@link PlayerId}
     */
    public PlayerId getId() { return id; }

    /**
     * Sets the unique identifier for this player.
     *
     * @param id the new {@link PlayerId} to assign
     */
    public void setId(PlayerId id) { this.id = id; }

    /**
     * Checks whether the given {@link PlayerId} matches this player's id.
     *
     * @param id the {@link PlayerId} to compare against
     * @return {@code true} if the ids match, {@code false} otherwise
     */
    public boolean matchId(PlayerId id) {
        return this.id.getId().compareTo(id.getId()) == 0;
    }

    /**
     * Returns the player's current hand of cards.
     *
     * @return the player's {@link Hand}
     */
    public Hand getPlayerHand() { return playerHand; }

    /**
     * Adds a single card to this player's hand.
     *
     * @param c the {@link Card} to add
     */
    public void addCardToHand(Card c) {
        this.playerHand.addCard(c);
    }

    /**
     * Returns the player's current chip balance.
     *
     * @return the balance as a non-negative integer
     */
    public int getBalance() { return balance; }

    /**
     * Deducts the given amount from the player's balance.
     *
     * @param amount the positive integer amount to subtract
     */
    public void subtractBalance(int amount) { setBalance(this.balance - amount); }

    protected void setBalance(int balance) {
        var oldVal = this.balance;
        this.balance = balance;
        pcs.firePropertyChange("balance",oldVal,this.balance);
    }

    /**
     * Returns the minimum bet size this player is required to place.
     * This is typically set to half the current blind.
     *
     * @return the minimum bet amount
     */
    public int getMinBet() { return minBet; }

    /**
     * Sets the minimum bet size for this player.
     *
     * @param minBet the new minimum bet amount (typically half the blind size)
     */
    public void setMinBet(int minBet) { this.minBet = minBet; }

    /**
     * Returns this player's running record of bets made during the current round.
     *
     * @return the player's {@link RoundInvestment}
     */
    public RoundInvestment getRoundInvestment() { return roundInvestment; }

    /**
     * Returns the total amount this player has invested across all pots in the current round.
     *
     * @return cumulative chips placed into pots this round
     */
    public int getTotalInvestment() { return roundInvestment.getTotalInvestment(); }

    /**
     * Returns the total amount this player has invested specifically in the given pot.
     *
     * @param pot the {@link Pot} whose investment total is requested
     * @return the sum of all bets made by this player into {@code pot}
     */
    public int getTotalPotInvestment(Pot pot) {
        ArrayList<Bet> betsInPot = roundInvestment.getBetsByPot(pot);
        int potInvestment = 0;
        if (betsInPot.isEmpty()) return 0;
        for (Bet b : betsInPot) {
            potInvestment += b.getBetSize();
        }
        return potInvestment;
    }

    protected void setRoundInvestment(int totalInvested) { this.roundInvestment = new RoundInvestment(totalInvested); }

    /**
     * Returns the player's current action (e.g. CALL, RAISE, FOLD, UNDECIDED).
     *
     * @return the current {@link Action}
     */
    public Action getAction() { return action; }

    /**
     * Sets the player's current action and fires a property change event.
     *
     * @param action the new {@link Action} to assign
     */
    public void setAction(Action action) {
        // Fire a change Action event
        var oldVal = this.action;
        this.action = action;
        pcs.firePropertyChange("action",oldVal,this.action);
    }

    /**
     * Returns whether it is currently this player's turn to act.
     *
     * @return {@code true} if this player must make a decision, {@code false} otherwise
     */
    public boolean getIsTurn() { return isTurn; }

    /**
     * Sets whether it is this player's turn to act and fires a property change event.
     *
     * @param isTurn {@code true} to signal that this player must act, {@code false} otherwise
     */
    public void setIsTurn(boolean isTurn) {
        // Fire a change isTurn event
        var oldVal = this.isTurn;
        this.isTurn = isTurn;
        pcs.firePropertyChange("isTurn",oldVal,this.isTurn);
    }

    /**
     * Returns the role this player holds in the current round (e.g. DEALER, SMALLBLIND, BIGBLIND).
     *
     * @return the current {@link Roles} value
     */
    public Roles getRole() { return role; }

    /**
     * Sets the player's role and fires a property change event.
     *
     * @param role the new {@link Roles} to assign
     */
    public void setRole(Roles role) {
        // Fire a change Role event
        var oldVal = this.role;
        this.role = role;
        pcs.firePropertyChange("role",oldVal,this.role);
    }

    /**
     * Returns the amount this player has declared they will bet this turn, or {@code null}
     * if no bet has been set yet.
     *
     * @return the active bet as an {@link Integer}, or {@code null}
     */
    public Integer getActiveBet() { return activeBet; }

    /**
     * Sets the amount this player will bet this turn.
     * The value is rounded to the nearest multiple of 5 unless the player is going all-in,
     * in which case it is forced to the player's full balance.
     *
     * @param activeBet the desired bet amount, or {@code null} to clear the bet
     */
    public void setActiveBet(Integer activeBet) {
        if (!action.equals(Action.ALLIN)) {
            if (activeBet != null && activeBet % 5 != 0 ) {
                activeBet = Math.round((float) activeBet / 5) * 5;
            }
        } else {
            activeBet = this.balance;
        }
        this.activeBet = activeBet;

    }

    /**
     * Resets this player's state at the start of a new round.
     * Clears the hand, sets action to UNDECIDED, clears the round investment, and turns off the active-turn flag.
     * Also fires a "roundReset" property change event.
     */
    public void roundReset() {
        pcs.firePropertyChange("roundReset",this, new Player(getName(),getBalance()));
        this.playerHand.clear();
        this.isTurn = false;
        this.action = Action.UNDECIDED;
        this.roundInvestment.reset();
        this.activeBet = null;
    }

    /**
     * Awards the player the chips they have won from a pot.
     *
     * @param potSize the total number of chips won (the pot's share for this player)
     */
    public void win(int potSize) {
        this.balance += potSize;
    }

    /**
     * Places a bet of the given size from this player's balance into the specified pot.
     * If the bet equals the player's entire balance it automatically triggers an all-in.
     *
     * @param betSize the number of chips to bet; must be &gt; 0 (or 0 only for CALL actions)
     * @param pot     the {@link Pot} the chips are placed into
     * @return the actual number of chips placed (maybe capped at balance for all-in)
     * @throws IllegalArgumentException if betSize is negative or exceeds balance without an ALLIN action
     */
    public int placeBet(int betSize, Pot pot) {
        int b = getBalance();

        if (betSize < 0 || (betSize == 0 && !this.action.equals(Action.CALL))) {
            this.action = Action.FOLD;
            return 0;
        }

        if (betSize > this.balance) {
            if (this.action.equals(Action.CALL)) {
                betSize = this.balance;
            } else if (!this.action.equals(Action.ALLIN)) throw new IllegalArgumentException(
                    "Bet must be <= balance | betSize=" + betSize +
                            " balance=" + this.balance +
                            " action=" + this.action +
                            " activeBet=" + this.activeBet
            );
            else betSize = this.balance;
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

    /**
     * Pays the blind obligation for this player based on their current role.
     * Does nothing if the player is {@code PLAYER} or {@code DEALER}.
     *
     * @param blindSize the base blind size from the game configuration
     * @param pot       the {@link Pot} the blind chips are placed into
     * @return the number of chips actually paid as the blind
     */
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

    /**
     * Commits all remaining chips as an all-in bet.
     * Subtracts the full balance but does not route the chips into a pot directly;
     * pot assignment occurs via {@link #placeBet} during the betting phase.
     */
    public void allIn() {
        int betAmount = this.balance;
        if (betAmount > 0) {
            subtractBalance(betAmount);
        }
    }

    /**
     * Marks the player as having forfeited the game.
     * Only takes effect if the player has already folded; changes the action from FOLD to FORFEIT.
     */
    public void forfeitGame() {
        // Send current balance to database and exit game.
        if (this.action.equals(Action.FOLD)) {
            setAction(Action.FORFEIT);
        }

    }

    /**
     * Processes this player's chosen action against the given list of active pots.
     * Converts a RAISE that does not exceed the current call amount into the appropriate
     * CALL or CHECK action, and validates that bet actions carry a positive active bet.
     *
     * @param pots the current list of {@link Pot} objects so the required call amount can be
     *             computed via {@link PotUtil#getToCall}
     */
    public void play(ArrayList<Pot> pots) {

        int toCall = PotUtil.getToCall(pots, this);

        // UI raises are interpreted as "raise to" total for this pot.
        // Convert to an incremental contribution so repeat raises do not overcharge the player.
        int betContribution = activeBet == null ? 0 : activeBet;

        if (this.action == Action.RAISE) {
            if (this.activeBet == null || this.activeBet <= toCall) {
                this.action = toCall > 0 ? Action.CALL : Action.CHECK;
                betContribution = toCall;
            }
        }

        if (Action.isBet(action)) {
            this.activeBet = betContribution;
            if (this.activeBet <= 0) {
                throw new IllegalStateException("Bet action requires a positive active bet.");
            }
            this.activeBet = betContribution;
        }
    }
}
