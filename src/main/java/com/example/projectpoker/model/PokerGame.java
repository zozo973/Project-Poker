package com.example.projectpoker.model;

import com.example.projectpoker.Player;

import java.util.ArrayList;

public class PokerGame {


    public enum Action {
        BET,
        CHECK,
        FOLD;
    }

    private Stage stage;
    private int numPlayers;
    private int pot;
    private CardDeck deck;
    private ArrayList<Player> players;

    public PokerGame(ArrayList<Player> players) {
        this.stage = Stage.PREFLOP;
        this.numPlayers = players.size();
        this.deck = new CardDeck();
        this.players = players;
    }
}
