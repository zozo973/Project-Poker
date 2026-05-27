package com.example.projectpoker.model.game;

import java.util.ArrayList;

/**
 * Final snapshot of a completed poker round, capturing all events and state.
 * Created at the end of each round for persistence and review.
 */
public class RoundLog {
    private final ArrayList<RoundLogEntry> roundLog;
    private final ArrayList<Player> players;
    private final ArrayList<Card> communityCards;
    private final ArrayList<Pot> pots;
    private final int roundNumber;

    /** Constructor
     *      Creates a round log to be stored in the database.
     * @param roundLog: List of events during a round
     * @param players: List of players from round, including their cards.
     * @param communityCards: List of cards that all players can make there final hand from.
     * @param pots: List of all pots from the round.
     * @param roundNumber: Round number incremented from 1 to the total number of rounds in a game.
     */

    public RoundLog(ArrayList<RoundLogEntry> roundLog, ArrayList<Player> players, ArrayList<Card> communityCards, ArrayList<Pot> pots, int roundNumber) {
        this.roundLog = roundLog;
        this.players = players;
        this.communityCards = communityCards;
        this.pots = pots;
        this.roundNumber = roundNumber;
    }

    /**
     * Returns the sequence of actions and events that occurred during this round.
     *
     * @return the list of {@link RoundLogEntry} objects
     */
    public ArrayList<RoundLogEntry> getRoundLog() { return roundLog; }

    /**
     * Returns all players involved in this round, including their final hole cards.
     *
     * @return the list of {@link Player} objects
     */
    public ArrayList<Player> getPlayers() { return players; }

    /**
     * Returns the community cards that were on the board at the end of this round.
     *
     * @return the list of {@link Card} objects
     */
    public ArrayList<Card> getCommunityCards() { return communityCards; }

    /**
     * Returns all pots (main pot and side pots) that existed in this round.
     *
     * @return the list of {@link Pot} objects
     */
    public ArrayList<Pot> getPots() { return pots; }

    /**
     * Returns the sequence number of this round in the game.
     *
     * @return the round number, typically starting from 1
     */
    public int getRoundNumber() { return roundNumber; }
}
