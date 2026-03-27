package com.example.projectpoker.model;

import java.util.ArrayList;

public class PokerRound {

    private Stage stage;
    private int numPlayers;
    private Pot pot;
    private int toPlay;
    private CardDeck deck;
    private ArrayList<GameLogEntry> roundLog;
    private ArrayList<Player> turnOrder;


    public PokerRound(ArrayList<Player> players, int blindSize, int[] roleIndices) {
        this.stage = Stage.PREFLOP;
        this.numPlayers = players.size();
        this.deck = new CardDeck();
        this.toPlay = blindSize;
        this.pot = new Pot(players);
        createTurnOrder(players,roleIndices);
    }

    public ArrayList<GameLogEntry> getRoundLog() { return roundLog; }

    private void createTurnOrder(ArrayList<Player> players, int[] roleIndices) {
        ArrayList<Player> turnOrder = new ArrayList<>();
        turnOrder.add(players.get(roleIndices[1]));
        for (int i = roleIndices[2]; i < numPlayers; i++){
            turnOrder.add(players.get(i));
        }
        for (int i = 0 ; i <= roleIndices[0]; i++){
            turnOrder.add(players.get(i));
        }
        this.turnOrder = turnOrder;
    }

    // TODO Attach to the game controller
    // On click method depending on player choice
    public void playerAction(Player player, int betSize, Action action) {
        if (action.isBet(action)) {
            this.roundLog.add(new GameLogEntry(player,toPlay-player.getRoundInvestment(),betSize,action));
        } else {
            this.roundLog.add(new GameLogEntry(player,betSize));
        }
    }

    private void dealCards() {
        for (int repeat = 0; repeat < 2; repeat++) {
            for (int i = 0; i < turnOrder.size(); i++) {
                turnOrder.get(i).addCardToHand(deck.draw());
            }
        }
        this.pot.addToPot(turnOrder.get(0).payBlind(toPlay) + turnOrder.get(1).payBlind(toPlay));
    }

    private void playPreFlop() {
        for (int i = 0; i < turnOrder.size(); i++) {
            turnOrder.get(i).setIsTurn(true);
            while (turnOrder.get(i).getIsTurn()) {
                // Wait for input on bet or check or fold
                // call method to asked user for input
                // turnOrder.get(i).setAction(actionButtonInput());
                int betSize = turnOrder.get(i).chooseBetSize();
                this.pot.addToPot(turnOrder.get(i).placeBet(betSize));
                playerAction(turnOrder.get(i),betSize,turnOrder.get(i).getAction());
                if (turnOrder.get(i).getAction() == Action.RAISE) this.toPlay = turnOrder.get(i).getRoundInvestment();

            }
        }
    }

    public void playRound() {
        dealCards();



    }
}
