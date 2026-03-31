package com.example.projectpoker.model.game;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class Pot {
    // private ArrayList<Player> players;
    private Dictionary<Player,Integer> betTable;
    private int potSize;

    public Pot(ArrayList<Player> players) {
        this.betTable = new Hashtable<>();
        this.potSize = 0;
    }

    public int getPotSize() { return potSize; }

    public void setPotSize(int potSize) { this.potSize = potSize; }

    private void initBetTable(ArrayList<Player> players) {
        for (Player p : players) {
            this.betTable.put(p,0);
        }
    }

    private void addBet2Table(Player player, int bet) {
        int currentBets = betTable.get(player);
        this.betTable.put(player,currentBets+bet);
        if (betTable.get(player) == player.getRoundInvestment()) {
            System.err.println(player.getName() + "'s bets does not match their round investments");
        }
    }

    public void addBet(Player player, int bet) {
        player.placeBet(bet);
        addBet2Table(player,bet);
        this.potSize += bet;
    }

    // Pay and add small and big blinds to pot
    public void initBlinds(ArrayList<Player> players, ArrayList<Integer> turnOrder, int blindSize) {
        int smallBlind = players.get(turnOrder.get(0)).payBlind(blindSize);
        int bigBlind = players.get(turnOrder.get(1)).payBlind(blindSize);
        addBet2Table(players.get(turnOrder.get(0)),smallBlind);
        addBet2Table(players.get(turnOrder.get(1)),bigBlind);
        this.potSize = smallBlind + bigBlind;
    }
}
