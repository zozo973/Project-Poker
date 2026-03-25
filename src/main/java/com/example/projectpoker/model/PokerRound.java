package com.example.projectpoker.model;

import com.example.projectpoker.Player;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class PokerRound {

    private Stage stage;
    private int numPlayers;
    private int pot;
    private CardDeck deck;
    private Dictionary<Player,Integer> betLog;


    public PokerRound(ArrayList<Player> players, ArrayList<Roles> roles, int blindSize) {
        this.stage = Stage.PREFLOP;
        this.numPlayers = players.size();
        this.deck = new CardDeck();
        Dictionary<Player,Integer> betLog = new Hashtable<>();
        for (int i = 0; i < players.size();i++) {
            int bet = (int) (roles.get(i).getBlindMultiplier()*blindSize);
            betLog.put(players.get(i),bet);
        }
        this.betLog = betLog;
    }


}
