package com.example.projectpoker.model.game;

import java.util.ArrayList;

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
}
