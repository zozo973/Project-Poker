package com.example.projectpoker.model.game;

import com.example.projectpoker.model.HandEvaluation;
import com.example.projectpoker.model.PlayerResult;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Roles;
import com.example.projectpoker.model.game.enums.RoundStatus;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class Pot {
    private ArrayList<Player> players;
    private Dictionary<Player,Integer> betTable;
    private int potSize;
    private int toPlay;
    private int investmentPP;
    private boolean isOpen;
    private int potPriority;

    public Pot() {
        this.players = new ArrayList<>();
        this.betTable = new Hashtable<>();
        this.potSize = 0;
        this.toPlay = 0;
        this.investmentPP = 0;
        this.isOpen = true;
        this.potPriority = 0;
    }

    public Pot(Player player) {
        this.players = new ArrayList<>();
        this.players.add(player);
        this.potSize = 0;
        this.potPriority = 0;
        this.investmentPP = 0;
        this.toPlay = 0;
        this.isOpen = true;
        initBetTable();
    }

    public Pot(Player player, int potPriority) {
        this.players = new ArrayList<>();
        this.players.add(player);
        this.potSize = 0;
        this.potPriority = potPriority;
        this.investmentPP = 0;
        this.toPlay = 0;
        this.isOpen = true;
        initBetTable();
    }

    public Pot(ArrayList<Player> players) {
        this.players = new ArrayList<>(players);
        this.potSize = 0;
        this.potPriority = 0;
        this.investmentPP = 0;
        this.toPlay = 0;
        this.isOpen = true;
        initBetTable();
    }

    public ArrayList<Player> getPlayers() { return players; }

    public void addPlayer(Player player) {
        this.players.add(player);
        addPlayer2Table(player);
    }

    public void setPlayers(ArrayList<Player> players) { this.players = players; }

    public int getToPlay() { return toPlay; }

    public int getToPlay(Player p) {
        if (!this.players.contains(p)) addPlayer(p);
        return this.investmentPP - p.getTotalPotInvestment(this);
    }

    public void setToPlay(int toPlay) { this.toPlay = toPlay; }

    public int getInvestmentPP() { return investmentPP; }

    private void setInvestmentPP() {
        int largestInvestment = 0;
        for (Player p : this.players) {
            if (betTable.get(p) > largestInvestment) largestInvestment = betTable.get(p);
        }
        this.investmentPP = largestInvestment; }

    public void setInvestmentPP(int investmentPP) { this.investmentPP = investmentPP; }

    public void stepPotPriority(int step) { this.potPriority += step; }

    public int getPotPriority() { return potPriority; }

    public void setPotPriority(int potPriority) { this.potPriority = potPriority; }

    public int getPotSize() { return potSize; }

    public void setPotSize(int potSize) { this.potSize = potSize; }

    public boolean getIsOpen() { return isOpen; }

    public void setIsOpen(boolean status) {
        if (!status) setPotPriority(-1);
        this.isOpen = status;
    }

    public void closePot() {
        this.isOpen = false;
        this.toPlay = 0;
    }

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
        if (!this.players.contains(player)) {
            addPlayer(player);
            this.betTable.put(player,bet);
        } else {
            int currentBets = betTable.get(player);
            this.betTable.put(player,currentBets+bet);
        }
    }

    public void addBet(Player player, int bet) {
        addBet2Table(player, bet);
        player.placeBet(bet, this);

        setInvestmentPP();

        if (this.toPlay == 0 && bet > 0) this.toPlay = bet;
        if (bet >= this.toPlay && Action.isRaise(player.getAction())) this.toPlay = investmentPP;

        this.potSize += bet;
    }

    // Pay and add small and big blinds to pot
    public void initBlinds(ArrayList<Player> players, ArrayList<Integer> turnOrder, int blindSize) {
        int smallBlind = players.get(turnOrder.get(0)).payBlind(blindSize,this);
        int bigBlind = players.get(turnOrder.get(1)).payBlind(blindSize,this);
        addBet2Table(players.get(turnOrder.get(0)),smallBlind);
        addBet2Table(players.get(turnOrder.get(1)),bigBlind);
        this.potSize = smallBlind + bigBlind;
        this.toPlay = Math.max(smallBlind, bigBlind);
        setInvestmentPP();
    }

    public RoundStatus removeFolded(RoundStatus status) {
        players.removeIf(p -> p.getAction() == Action.FOLD);

        if (players.size() == 1) {
            return RoundStatus.SHOWDOWN;
        }
        return RoundStatus.stepRoundStatus(status);
    }

    public int showDown(ArrayList<Card> communityCards) {
        ArrayList<PlayerResult> gameResults;
        gameResults = HandEvaluation.whoWins(communityCards, this.players);
        int numWinners = gameResults.size();
        for (PlayerResult gameResult : gameResults) {
            for (Player p : players) {
                if (p.matchId(gameResult.getPlayerId())) {
                    p.win(potSize / numWinners);
                    p.setRole(Roles.WINNER);
                    break;
                }
            }
        }
        return numWinners;
    }

    public void adjustPot(Pot sidePot) {
            if (sidePot.getPotPriority() < this.potPriority) {
                int removeInvestmentPP = sidePot.getInvestmentPP() - this.investmentPP;
                int removePotTotal = this.potSize - removeInvestmentPP*players.size();
                if (this.potSize <= 0) throw new IllegalStateException("THe pot can't have 0 or negative amount of money in it");
                if (removeInvestmentPP <= 0 ) throw new IllegalStateException("adjustPot Method has been implemented on the incorrect pot");
                reInitPot(removeInvestmentPP,removePotTotal);

                sidePot.initPot(this.players);
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

    private void initPot(ArrayList<Player> players) {
        for (Player p : players) {
            if (!this.players.contains(p)) {
                addPlayer(p);
                addPlayer2Table(p,this.investmentPP);
                this.potSize += this.investmentPP;
                p.getRoundInvestment().reInit(this);
            }
        }
    }
}
