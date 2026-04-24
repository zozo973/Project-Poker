package com.example.projectpoker.model.game;

import com.example.projectpoker.model.HandEvaluation;
import com.example.projectpoker.model.HandResult;
import com.example.projectpoker.model.PlayerResult;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.RoundStatus;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class Pot {
    private ArrayList<Player> players;
    private Dictionary<Player,Integer> betTable;
    private int potSize;
    private int toPlay;
    private boolean isOpen;
    private int potPriority;

    public Pot() {
        this.players = new ArrayList<>();
        this.betTable = new Hashtable<>();
        this.potSize = 0;
        this.toPlay = 0;
        this.isOpen = true;
        this.potPriority = 0;
    }

    public Pot(Player player) {
        this.players = new ArrayList<>();
        this.players.add(player);
        this.potSize = 0;
        this.potPriority = 0;
        this.toPlay = 0;
        this.isOpen = true;
        initBetTable();
    }

    public Pot(Player player, int potPriority) {
        this.players = new ArrayList<>();
        this.players.add(player);
        this.potSize = 0;
        this.potPriority = potPriority;
        this.toPlay = 0;
        this.isOpen = true;
        initBetTable();
    }

    public Pot(ArrayList<Player> players) {
        this.players = players;
        this.potSize = 0;
        this.potPriority = 0;
        this.toPlay = 0;
        this.isOpen = true;
        initBetTable();
    }

    public ArrayList<Player> getPlayers() { return players; }

    public void addPlayer(Player player) { this.players.add(player); }

    public void setPlayers(ArrayList<Player> players) { this.players = players; }

    public int getToPlay() { return toPlay; }

    public void setToPlay(int toPlay) { this.toPlay = toPlay; }

    public void stepPotPriority(int step) { this.potPriority += step; }

    public int getPotPriority() { return potPriority; }

    public void setPotPriority(int potPriority) { this.potPriority = potPriority; }

    public int getPotSize() { return potSize; }

    public void setPotSize(int potSize) { this.potSize = potSize; }

    public boolean getIsOpen() { return isOpen; }

    public void setIsOpen(boolean status) {
        this.isOpen = status;
        this.potPriority = -1;
    }

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
        // if (betTable.get(player) != player.getRoundInvestment().getTotalInvestment()) {
        //     System.err.println(player.getName() + "'s bets does not match their round investments");
        // }
    }

    public void addBet(Player player, int bet) {
        player.placeBet(bet, this);
        int playerPotInvest = player.getTotalPotInvestment(this);
        if (playerPotInvest > this.toPlay) this.toPlay = playerPotInvest;
        addBet2Table(player, bet);

        //     if (Action.isRaise(player.getAction()) && player.getRoundInvestment() > this.toPlay) {
        //        this.toPlay = player.getTotalRoundInvestment();
        //          }
        this.potSize += bet;
    }

    public void addBet(Player player) {
        int bet = player.getActiveBet();
        player.placeBet(bet, this);
        int playerPotInvest = player.getTotalPotInvestment(this);
        if (playerPotInvest>this.toPlay) this.toPlay = playerPotInvest;
        addBet2Table(player,bet);

   //     if (Action.isRaise(player.getAction()) && player.getRoundInvestment() > this.toPlay) {
   //        this.toPlay = player.getTotalRoundInvestment();
   //          }
        this.potSize += bet;
    }

    // Pay and add small and big blinds to pot
    public void initBlinds(ArrayList<Player> players, ArrayList<Integer> turnOrder, int blindSize) {
        int smallBlind = players.get(turnOrder.get(0)).payBlind(blindSize,this);
        int bigBlind = players.get(turnOrder.get(1)).payBlind(blindSize,this);
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

    public void showDown(ArrayList<Card> communityCards) {
        ArrayList<PlayerResult> gameResults = new ArrayList<>();
        gameResults = HandEvaluation.whoWins(communityCards, this.players);
        int numWinners = gameResults.size();
        for (PlayerResult gameResult : gameResults) {
            for (Player p : players) { /// break possibly breaks out of both loops, requires testing
                if (p.matchId(gameResult.getPlayerId())) p.win(potSize / numWinners);
                break;
            }
        }
    }

    public void adjustPot(Pot sidePot) {
        if (sidePot.isOpen) {
            if (sidePot.getPotPriority() < this.potPriority) {
                int removeToPlay = sidePot.getToPlay() - this.toPlay;
                int removePotTotal = this.potSize - removeToPlay*players.size();
                if (this.potSize <= 0) throw new IllegalStateException("THe pot can't have 0 or negative amount of money in it");
                if (removeToPlay <= 0 ) throw new IllegalStateException("adjustPot Method has been implemented on the incorrect pot");
                reInitPot(removeToPlay,removePotTotal);
            }
        }
    }

    private void reInitPot(int removeToPlay,int removePotTotal) {
        this.toPlay-= removeToPlay;
        this.potSize-=removePotTotal;
        for (Player p : players) {
            int currentBets = this.betTable.get(p);
            this.betTable.put(p,currentBets-removePotTotal);
        }
    }

    public void payOut() {
        if (players.size() > 1) System.err.println("Payout method shouldn't be called if there is more then one player left.");
        players.getFirst().win(potSize);
    }
}
