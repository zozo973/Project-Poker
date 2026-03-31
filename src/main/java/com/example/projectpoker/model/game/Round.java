package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.oberserver.AbsSubject;

import java.util.ArrayList;

public class Round extends AbsSubject {

    private RoundStatus state;
    private int numPlayers;
    private Pot pot;
    private int toPlay;
    private CardDeck deck;
    private ArrayList<Card> communityCards;
    private ArrayList<GameLogEntry> roundLog;
    private ArrayList<Player> players;
    private ArrayList<Integer> turnOrder;


    public Round(ArrayList<Player> players, int blindSize) {
        this.players = players;
        this.toPlay = blindSize;
        this.numPlayers = players.size();
        this.communityCards = new ArrayList<>();
        this.deck = new CardDeck();
        this.pot = new Pot(players);
        this.turnOrder = new ArrayList<>();
    }

    public ArrayList<GameLogEntry> getRoundLog() { return roundLog; }

    public void Init() {
        createTurnOrder(RoleUtil.findRoleIndices(players));
        for (Player p : players) addObserver(p);
        this.state = RoundStatus.BLINDS;
        this.pot.initBlinds(players,turnOrder,toPlay);
        notifyObservers(state);
    }

    public void Start() {
        this.state = RoundStatus.DEAL;
        dealCards();
        notifyObservers(state);

        // wait

        this.state = RoundStatus.BETTING1;
        notifyObservers(state);
        checkBetType();

        this.state = RoundStatus.FLOP;
        deal2Table();
        notifyObservers(state);

        this.state = RoundStatus.BETTING2;
        notifyObservers(state);
        checkBetType();

        this.state = RoundStatus.TURN;
        deal2Table();
        notifyObservers(state);

        this.state = RoundStatus.BETTING3;
        notifyObservers(state);
        checkBetType();

        this.state = RoundStatus.RIVER;
        deal2Table();
        notifyObservers(state);

        this.state = RoundStatus.SHOWDOWN;
        notifyObservers(state);
        checkBetType();
    }




    private void createTurnOrder(int[] roleIndices) {
        turnOrder.add((roleIndices[1]));
        for (int i = roleIndices[2]; i < numPlayers; i++){
            turnOrder.add(i);
        }
        for (int i = 0 ; i <= roleIndices[0]; i++){
            turnOrder.add(i);
        }
    }

    // TODO Attach to the game controller
    // On click method depending on player choice
    public void playerAction(Player player, int betSize) {
        Action action = player.getAction();
        if (action.isBet(action)) {
            this.roundLog.add(new GameLogEntry(player,toPlay-player.getRoundInvestment(),betSize,action));
        } else {
            this.roundLog.add(new GameLogEntry(player,betSize));
        }
    }

    private void dealCards() {
        for (int repeat = 0; repeat < 2; repeat++) {
            for (int i = 0; i < turnOrder.size(); i++) {
                players.get(turnOrder.get(i)).addCardToHand(deck.draw());
            }
        }
    }

    private void deal2Table() {
        deck.burnCard();
        if(state == RoundStatus.FLOP) {
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
        } else {
            this.communityCards.add(deck.draw());
        }
    }

    private void betting() {
        for (int i = 0; i < turnOrder.size(); i++) {
            players.get(turnOrder.get(i)).setIsTurn(true);
            while (players.get(turnOrder.get(i)).getIsTurn()) {
                // Wait for input on bet or check or fold
                // call method to asked user for input
                // turnOrder.get(i).setAction(actionButtonInput());
                int betSize = players.get(turnOrder.get(i)).chooseBetSize();
                this.pot.addBet(players.get(turnOrder.get(i)),betSize);
                playerAction(players.get(turnOrder.get(i)),betSize);
                if (players.get(turnOrder.get(i)).getAction() == Action.RAISE) {
                    this.toPlay = players.get(turnOrder.get(i)).getRoundInvestment();
                }
            }
        }
    }

    public void checkBetType() {

     //   switch ()
        betting();


    }
}
