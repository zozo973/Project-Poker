package com.example.projectpoker.model.game;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class Pot {
    private ArrayList<Player> players;
    private Dictionary<Player,Integer> betTable;
    private int potSize;
    private boolean isOpen;

    public Pot() {
        this.players = new ArrayList<>();
        this.betTable = new Hashtable<>();
        this.potSize = 0;
        this.isOpen = true;
    }

    public Pot(ArrayList<Player> players) {
        this.players = players;
        this.potSize = 0;
        this.isOpen = true;
        initBetTable();
    }

    public int getPotSize() { return potSize; }

    public void setPotSize(int potSize) { this.potSize = potSize; }

    public boolean getIsOpen() { return isOpen; }

    public void closePot() { this.isOpen = false; }

    private void initBetTable() {
        this.betTable = new Hashtable<>();
        for (Player p : players) {
            this.betTable.put(p,0);
        }
    }

    public void addPlayer2Table(Player player) {
        this.betTable.put(player,0);
    }

    public void addPlayer2Table(Player player, int bet) {
        this.betTable.put(player,bet);
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

    public RoundStatus removeFolded(RoundStatus status) {
        players.removeIf(p -> p.getAction() == Action.FOLD);
        if (players.size() == 1) {
            players.getFirst().win(potSize);
            return RoundStatus.END;
        }
        return RoundStatus.stepRoundStatus(status);
    }

    public void showDown() {
        // TODO add hand evaluation methods
        Player winner = new Player(); // replace new instance with hand eval methods
        winner.win(potSize);
    }

    public void payOut() {
        if (players.size() > 1) System.err.println("Payout method shouldn't be called if there is more then one player left.");
        players.getFirst().win(potSize);
    }
}
