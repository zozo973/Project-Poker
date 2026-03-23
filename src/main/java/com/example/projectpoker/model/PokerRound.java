package com.example.projectpoker.model;

import com.example.projectpoker.Player;

import java.util.ArrayList;

public class PokerRound {

    private Stage stage;
    private int numPlayers;
    private int pot;
    private CardDeck deck;


    public PokerRound(ArrayList<Player> players) {
        this.stage = Stage.PREFLOP;
        this.numPlayers = players.size();
        this.deck = new CardDeck();
        this.players = players;
    }
}
