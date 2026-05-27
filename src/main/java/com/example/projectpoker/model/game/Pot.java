package com.example.projectpoker.model.game;

import com.example.projectpoker.model.HandEvaluation;
import com.example.projectpoker.model.PlayerResult;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Roles;
import com.example.projectpoker.model.game.enums.RoundStatus;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class Pot {
    private ArrayList<Player> players;
    private Dictionary<Player,Integer> betTable;
    private int potSize;
    private int toPlay;
    private int investmentPP;
    private boolean isOpen;
    private int potPriority;

    public Pot() {
        this.players = new ArrayList<>();
        this.betTable = new Hashtable<>();
        this.potSize = 0;
        this.toPlay = 0;
        this.investmentPP = 0;
        this.isOpen = true;
        this.potPriority = 0;
    }

    public Pot(Player player) {
        this.players = new ArrayList<>();
        this.players.add(player);
        this.potSize = 0;
        this.potPriority = 0;
        this.investmentPP = 0;
        this.toPlay = 0;
        this.isOpen = true;
        initBetTable();
    }

    public Pot(Player player, int potPriority) {
        this.players = new ArrayList<>();
        this.players.add(player);
        this.potSize = 0;
        this.potPriority = potPriority;
        this.investmentPP = 0;
        this.toPlay = 0;
        this.isOpen = true;
        initBetTable();
    }

    public Pot(ArrayList<Player> players) {
        this.players = new ArrayList<>(players);
        this.potSize = 0;
        this.potPriority = 0;
        this.investmentPP = 0;
        this.toPlay = 0;
        this.isOpen = true;
        initBetTable();
    }

    /**
     * Returns the list of players currently eligible to win this pot.
     *
     * @return the mutable {@link ArrayList} of participating {@link Player} objects
     */
    public ArrayList<Player> getPlayers() { return players; }

    /**
     * Adds a player to this pot and registers them in the bet-tracking table with a zero contribution.
     *
     * @param player the {@link Player} to add
     */
    public void addPlayer(Player player) {
        this.players.add(player);
        addPlayer2Table(player,0);
    }

    /**
     * Replaces the entire list of eligible players for this pot.
     *
     * @param players the new list of {@link Player} objects
     */
    public void setPlayers(ArrayList<Player> players) { this.players = players; }

    /**
     * Returns the total amount required from each player to stay in this pot this betting round.
     *
     * @return the current amount-to-play for the pot as a whole
     */
    public int getToPlay() { return toPlay; }

    /**
     * Returns the amount a specific player still needs to contribute to match the highest bet in this pot.
     * Adds the player to this pot if they are not already registered.
     *
     * @param p the {@link Player} whose remaining contribution is calculated
     * @return the positive difference between the highest investment per player and what {@code p} has invested
     */
    public int getToPlay(Player p) {
        if (!this.players.contains(p)) addPlayer(p);
        return this.investmentPP - p.getTotalPotInvestment(this);
    }

    /**
     * Sets the amount-to-play for this pot (used during blind initialisation and pot adjustments).
     *
     * @param toPlay the new amount every player must match
     */
    public void setToPlay(int toPlay) { this.toPlay = toPlay; }

    /**
     * Returns the current maximum chips any single player has invested in this pot.
     *
     * @return the investment-per-player ceiling
     */
    public int getInvestmentPP() { return investmentPP; }

    private void setInvestmentPP() {
        int largestInvestment = 0;
        for (Player p : this.players) {
            if (betTable.get(p) > largestInvestment) largestInvestment = betTable.get(p);
        }
        this.investmentPP = largestInvestment;
    }

    /**
     * Manually overrides the investment-per-player value (used during side-pot adjustments).
     *
     * @param investmentPP the new per-player investment ceiling
     */
    public void setInvestmentPP(int investmentPP) { this.investmentPP = investmentPP; }

    /**
     * Increments this pot's priority value by the given step amount.
     * Higher priority means the pot was created later (side pot ordering).
     *
     * @param step the integer amount to add to the current priority
     */
    public void stepPotPriority(int step) { this.potPriority += step; }

    /**
     * Returns the priority index of this pot.
     * Lower values are processed first; side pots have higher values.
     *
     * @return the pot priority integer
     */
    public int getPotPriority() { return potPriority; }

    /**
     * Sets the priority index of this pot.
     *
     * @param potPriority the new priority value
     */
    public void setPotPriority(int potPriority) { this.potPriority = potPriority; }

    /**
     * Returns the total number of chips currently in this pot.
     *
     * @return the pot size in chips
     */
    public int getPotSize() { return potSize; }

    /**
     * Directly overrides the total size of this pot.
     *
     * @param potSize the new pot value in chips
     */
    public void setPotSize(int potSize) { this.potSize = potSize; }

    /**
     * Increases the pot size by the given amount.
     *
     * @param amount the positive number of chips to add
     */
    public void addToPotSize(int amount) { this.potSize += amount; }

    /**
     * Returns whether this pot is still open for betting.
     *
     * @return {@code true} if players can still bet into this pot; {@code false} if it is closed
     */
    public boolean getIsOpen() { return isOpen; }

    /**
     * Opens or closes this pot.
     * Closing sets the pot priority to {@code -1} to signal that it is no longer the active pot.
     *
     * @param status {@code true} to open the pot (accepting bets), {@code false} to close it
     */
    public void setIsOpen(boolean status) {
        if (!status) setPotPriority(-1);
        this.isOpen = status;
    }

    /**
     * Closes this pot and resets the amount-to-play to zero, preventing further bets.
     */
    public void closePot() {
        this.isOpen = false;
        this.toPlay = 0;
    }

    private void initBetTable() {
        this.betTable = new Hashtable<>();
        for (Player p : players) {
            this.betTable.put(p,0);
        }
    }

    /**
     * Adds a player to the bet-tracking table without an existing contribution (initialised to zero).
     *
     * @param player the {@link Player} to register in the bet table
     */
    public void addPlayer2Table(Player player) {
        this.betTable.put(player,0);
    }

    /**
     * Adds a player to the bet-tracking table with a specific existing contribution.
     *
     * @param player the {@link Player} to register
     * @param bet    the amount already attributed to this player in the pot
     */
    public void addPlayer2Table(Player player, int bet) {
        this.betTable.put(player,bet);
    }

    /**
     * Returns the recorded bet contribution for a specific player from the bet table.
     *
     * @param player the {@link Player} whose contribution is looked up
     * @return the integer chip amount this player has contributed to this pot
     */
    public int getBetFromTable(Player player) { return this.betTable.get(player); }

    private void addBet2Table(Player player, int bet) {
        if (!this.players.contains(player)) {
            addPlayer(player);
            this.betTable.put(player,bet);
        } else {
            int currentBets = betTable.get(player);
            this.betTable.put(player,currentBets+bet);
        }
    }

    /**
     * Records a bet from the given player into this pot.
     * Updates the bet table, fires the player's {@link Player#placeBet} deduction,
     * recalculates the investment-per-player, and updates the amount-to-play when a raise occurs.
     *
     * @param player the {@link Player} placing the bet
     * @param bet    the number of chips being bet
     */
    public void addBet(Player player, int bet) {
        addBet2Table(player, bet);
        player.placeBet(bet, this);

        setInvestmentPP();

        if (this.toPlay == 0 && bet > 0) this.toPlay = bet;
        if (bet >= this.toPlay && Action.isRaise(player.getAction())) this.toPlay = investmentPP;

        this.potSize += bet;
    }

    /**
     * Deducts blind amounts from the small-blind and big-blind players and adds them to this pot.
     * Should be called exactly once per round, before any other bets.
     *
     * @param players   the full list of players in turn order
     * @param turnOrder a list of seat indices where index 0 = small blind, index 1 = big blind
     * @param blindSize the base blind amount (small blind = 0.5×, big blind = 1×)
     */
    public void initBlinds(ArrayList<Player> players, ArrayList<Integer> turnOrder, int blindSize) {
        int smallBlind = players.get(turnOrder.get(0)).payBlind(blindSize,this);
        int bigBlind = players.get(turnOrder.get(1)).payBlind(blindSize,this);
        addBet2Table(players.get(turnOrder.get(0)),smallBlind);
        addBet2Table(players.get(turnOrder.get(1)),bigBlind);
        this.potSize = smallBlind + bigBlind;
        this.toPlay = Math.max(smallBlind, bigBlind);
        setInvestmentPP();
    }

    /**
     * Removes all folded players from this pot's player list.
     * If only one player remains after removal the round is cut short and SHOWDOWN is returned.
     *
     * @param status the current {@link RoundStatus}; END is passed through unchanged
     * @return the next {@link RoundStatus} after removing folded players
     */
    public RoundStatus removeFolded(RoundStatus status) {
        players.removeIf(p -> p.getAction() == Action.FOLD);

        if (players.size() == 1) {
            return RoundStatus.SHOWDOWN;
        } else if (status.equals(RoundStatus.END)) return RoundStatus.END;

        return RoundStatus.stepRoundStatus(status);
    }

    /**
     * Evaluates all remaining hands at showdown, awards the pot to the winner(s),
     * and marks winning players with the WINNER role.
     *
     * @param communityCards the five community cards on the board
     * @return the number of winners who split the pot
     */
    public int showDown(ArrayList<Card> communityCards) {
        ArrayList<PlayerResult> gameResults;
        gameResults = HandEvaluation.whoWins(communityCards, this.players);
        int numWinners = gameResults.size();
        for (PlayerResult gameResult : gameResults) {
            for (Player p : players) {
                if (p.matchId(gameResult.getPlayerId())) {
                    p.win(potSize / numWinners);
                    p.setRole(Roles.WINNER);
                    break;
                }
            }
        }
        return numWinners;
    }

    /**
     * Migrates chips from this (higher-priority) pot into the given lower-priority side pot.
     * Recalculates each player's investment records to reflect the new pot hierarchy.
     *
     * @param sidePot the lower-priority {@link Pot} that should receive a portion of this pot's chips;
     *                must have a priority strictly less than this pot's priority
     * @throws IllegalStateException if the adjustment would produce a negative pot size
     */
    public void adjustPot(Pot sidePot) {
        if (sidePot.getPotPriority() < this.potPriority) {

            int removeInvestmentPP = sidePot.getInvestmentPP();
            if (this.investmentPP - sidePot.getInvestmentPP() < 0 ) throw new IllegalStateException("adjustPot Method has been implemented on the incorrect pot");
            this.investmentPP -= removeInvestmentPP;

            for (Player p : this.players) {
                int amountInvested = this.betTable.get(p);

                if (Action.isInGame(p.getAction())) {
                    if (!sidePot.getPlayers().contains(p)) sidePot.addPlayer(p);
                    if (amountInvested <= removeInvestmentPP && amountInvested != 0) {
                        this.potSize -= amountInvested;
                        this.betTable.put(p, 0);

                        sidePot.addBet2Table(p, amountInvested);
                        sidePot.addToPotSize(amountInvested);

                    } else if (amountInvested != 0) {
                        this.potSize -= removeInvestmentPP;
                        this.betTable.put(p, amountInvested - removeInvestmentPP);

                        sidePot.addBet2Table(p, removeInvestmentPP);
                        sidePot.addToPotSize(removeInvestmentPP);
                    }
                    p.getRoundInvestment().reInit(this);
                    p.getRoundInvestment().reInit(sidePot);
                }
            }
            if (this.potSize < 0) throw new IllegalStateException("THe pot can't have negative amount of money in it");
        }
    }
}
