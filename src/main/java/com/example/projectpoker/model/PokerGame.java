package com.example.projectpoker.model;

import com.example.projectpoker.Player;

import java.util.ArrayList;

public class PokerGame {
    public enum Round {
        PREFLOP("Pre-Flop"),
        FLOP("Flop"),
        TURN("Turn"),
        RIVER("River");

        private final String description;

        Round(String description) {
            this.description = description;
        }
    }

    public enum Action {
        BET,
        CHECK,
        FOLD;
    }

    private Round round;
    private int numPlayers;
    private CardDeck deck;
    private ArrayList<Player> players;

    public PokerGame(ArrayList<Player> players) {
        this.round = Round.PREFLOP;
        this.numPlayers = players.size();
        this.deck = new CardDeck();
        this.players = players;
    }
}
