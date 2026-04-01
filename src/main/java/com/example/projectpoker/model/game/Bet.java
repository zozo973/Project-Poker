package com.example.projectpoker.model.game;

import java.util.ArrayList;

public class Bet {
    private int betSize;
    private Player player;

    public Bet() {
        this.betSize = 0;
    }

    public Bet(Player player) {
        this.betSize = 0;
        this.player = player;
    }

    public Bet(int betSize, Player player) {
        this.betSize = betSize;
        this.player = player;
    }



    public int getBetSize() { return betSize; }

    public void setBetSize(int betSize) { this.betSize = betSize; }

    public Player getPlayer() { return player; }

    public void setPlayer(Player player) { this.player = player; }

    public int bets2Pot(ArrayList<Bet> bets) {
        int totalBets = 0;
        for (int i = 0; i < bets.size(); i++) {
            totalBets += bets.get(i).getBetSize();
        }
        return totalBets;
    }

    public int bets2Pot(ArrayList<Bet> bets, Pot mainPot, Pot sidePot) {
        int totalBets = 0;
        for (int i = 0; i < bets.size(); i++) {
            totalBets += bets.get(i).getBetSize();
        }
        return totalBets;
    }

     public boolean testAllIn() {
         if (betSize == player.getBalance()) {
             this.player.setAction(Action.ALLIN);
             return true;
         }
         return false;
     }

     public int findAllInIndex(ArrayList<Bet> bets) {
         for (int i = 0; i < bets.size(); i++) {
             if (bets.get(i).getPlayer().getAction() == Action.ALLIN) return i ;
         }
         return -1;
     }

     public Pot testCreateSidePot(ArrayList<Bet> bets) {
         int AllInIndex = findAllInIndex(bets);
         if (AllInIndex == -1) System.err.println("No one went all in");
         // TODO: replace with exception
         Pot sidePot = new Pot();
         int allInCall = bets.get(AllInIndex).getBetSize();
         for (int i = AllInIndex+1; i < bets.size()+AllInIndex;i++) {
             Player p = bets.get(i).getPlayer();
             int b = bets.get(i).getBetSize()-allInCall;
             if (Action.isBet(p.getAction()) && (b-allInCall)>0) {
                 sidePot.addPlayer2Table(p,b);
             }
         }
        return sidePot;
     }

     public boolean TestSkip2ShowDown(ArrayList<Bet> bets) {
        int numAllIn = 0;
        int numCall = 0;
        int numRaise = 0;
        for (Bet b : bets) {
             if (b.testAllIn()) {
                 numAllIn += 1;
             } else if (b.getPlayer().getAction() == Action.CALL) {
                 numCall += 1;
             } else if (b.getPlayer().getAction() == Action.RAISE) {
                 numRaise += 1;
             }
        }
        if (numRaise > 0) return false;
        if (numCall > 1) return false;
        if (bets.isEmpty()) return false;
        return true;
     }
}
