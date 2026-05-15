package com.example.projectpoker.model.game;

import java.util.ArrayList;

public class RoundLog {
    private final ArrayList<RoundLogEntry> roundLog;
    private final ArrayList<Player> players;
    private final ArrayList<Card> communityCards;
    private final ArrayList<Pot> pots;
    private final int roundNumber;

    public RoundLog(ArrayList<RoundLogEntry> roundLog, ArrayList<Player> players, ArrayList<Card> communityCards, ArrayList<Pot> pots, int roundNumber) {
        this.roundLog = roundLog;
        this.players = players;
        this.communityCards = communityCards;
        this.pots = pots;
        this.roundNumber = roundNumber;
    }
}
