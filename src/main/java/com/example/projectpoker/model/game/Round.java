package com.example.projectpoker.model.game;

import com.example.projectpoker.database.DatabaseManager;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.BetType;
import com.example.projectpoker.model.game.enums.RoundStatus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Round {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private RoundStatus roundStatus;
    private int numPlayers;
    private int toPlay;
    private BetType betType;
    private CardDeck deck;
    private ArrayList<Pot> pots;
    private ArrayList<Card> communityCards;
    private ArrayList<GameLogEntry> roundLog;
    private ArrayList<Player> players;
    private ArrayList<Integer> turnOrder;
    private final int gameSessionId;
    private final int roundNumber;
    private boolean persisted;

    public Round(ArrayList<Player> players, int blindSize) {
        this(players, blindSize, -1, 1);
    }

    public Round(ArrayList<Player> players, int blindSize, int gameSessionId, int roundNumber) {
        this.players = players;
        this.toPlay = blindSize;
        this.numPlayers = players.size();
        this.communityCards = new ArrayList<>();
        this.deck = new CardDeck();
        this.pots = new ArrayList<>();
        this.pots.add(new Pot(players));
        this.turnOrder = new ArrayList<>();
        this.betType = BetType.NORMAL;
        this.roundLog = new ArrayList<>();
        this.gameSessionId = gameSessionId;
        this.roundNumber = roundNumber;
        this.persisted = false;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public RoundStatus getRoundStatus() {
        return roundStatus;
    }

    public void setRoundStatus(RoundStatus roundStatus) {
        var oldVal = this.roundStatus;
        this.roundStatus = roundStatus;
        pcs.firePropertyChange("state",oldVal,this.roundStatus);
    }

    public Pot getMainPot() { return this.pots.getFirst(); }

    public Pot getOpenPot() { return pots.get(getOpenPotIndex()); }

    public int getToPlay() { return toPlay; }

    public void setToPlay(int toPlay) { this.toPlay = toPlay; }

    public ArrayList<GameLogEntry> removeRoundLog() { return roundLog; }

    public ArrayList<GameLogEntry> getRoundLog() { return roundLog; }

    public BetType getBetType() { return betType; }

    public int getRoundNumber() { return roundNumber; }

    public void init() {
        createTurnOrder(RoleUtil.findRoleIndices(players));

        // add observer to relay state changes to ui addObserver();

        this.pots.getFirst().initBlinds(players,turnOrder,toPlay);
        setRoundStatus(RoundStatus.BLINDS);
    }

    public void start() {
        dealCards();
        setRoundStatus(RoundStatus.DEAL);

        setRoundStatus(RoundStatus.BETTING1);
        checkBetType();

        setRoundStatus(RoundStatus.FLOP);
        deal2Table();

        setRoundStatus(RoundStatus.BETTING2);
        checkBetType();

        setRoundStatus(RoundStatus.TURN);
        deal2Table();

        setRoundStatus(RoundStatus.BETTING3);
        checkBetType();


        setRoundStatus(RoundStatus.RIVER);
        deal2Table();

        setRoundStatus(RoundStatus.SHOWDOWN);
        checkBetType();
    }

    public void End() {
        if (persisted) {
            return;
        }
        setRoundStatus(RoundStatus.END);
        DatabaseManager.recordRound(gameSessionId, this);
        persisted = true;

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
        if(roundStatus == RoundStatus.FLOP) {
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
        } else {
            this.communityCards.add(deck.draw());
        }
    }

    private void betting() {
        while (!endBetting()) {
            for (int i = 0; i < turnOrder.size(); i++) {
                Player activePlayer = players.get(turnOrder.get(i));
                if (activePlayer.getAction() != Action.FOLD) {
                    activePlayer.setIsTurn(true);
//                    if (! (activePlayer instanceof AiPlayer)) {
//                        pcs.firePropertyChange("isTurn",false,true);
//                    }
                    while (activePlayer.getIsTurn()) {
                        // Wait for input on bet or check or fold
                        // call method to asked user for input
                        // turnOrder.get(i).setAction(actionButtonInput());
                        int betSize = activePlayer.chooseBetSize();
                        int activePotIndex = getOpenPotIndex();
                        if (activePotIndex == -1) System.err.println("No active Pot"); // Replace with exeception.

                        this.pots.get(activePotIndex).addBet(activePlayer, betSize);
                        playerAction(activePlayer, betSize);
                        if (activePlayer.getAction() == Action.RAISE) {
                            this.toPlay = activePlayer.getRoundInvestment();
                        }
                    }
                }

                if (endBetting()) break;
            }
        }
        for (Player p : players) {
            if (p.getAction() != Action.FOLD) p.setAction(Action.UNDECIDED);
        }
    }

    public boolean endBetting() {
        int numAllIn = 0;
        int numCall = 0;
        int numRaise = 0;
        int numFold = 0;
        int numCheck = 0;
        for (Player p : players) {
            if (p.getAction() == Action.ALLIN) {
                numAllIn += 1;
            } else if (p.getAction() == Action.CALL) {
                numCall += 1;
            } else if (p.getAction() == Action.RAISE) {
                numRaise += 1;
            } else if (p.getAction() == Action.FOLD) {
                numFold += 1;
            } else if (p.getAction() == Action.CHECK) {
                numCheck += 1;
            }
        }
        // end betting condition 1 (Normal or All In):
        //      One player action is Raise or All In,
        //      All other players actions are call or fold.
        boolean cond1 = (numRaise == 1 || numAllIn == 0 ) || (numRaise == 0 || numAllIn == 1 ) && (players.size() - 1 - numCall - numFold) == 0;
        // end betting condition 2 (Normal or End):
        //      All players actions are check, call or fold.
        boolean cond2 = (players.size()-numCall-numCheck-numFold) == 0;
        // end betting condition 3 (End):
        //      One player action is not fold,
        //      All other players actions are fold.
        boolean cond3 = (players.size() - numFold) == 1;
        // end betting condition 4 (All In):
        //      Multiple players action is All In,
        //      no players action is a raise,
        //      All other players actions are call or fold.
        boolean cond4 = (players.size()-numCall-numAllIn-numFold) == 0;
        // end betting condition 5 (SidePot):
        //      One or more players action is all in,
        //      One players action is to raise,
        //      All other players actions are call or fold.
        boolean cond5 = (numRaise == 1) && (numAllIn >= 1) && (players.size() - numFold - numCall - numAllIn - numRaise) == 0;
        if (cond5) {
            ArrayList<Player> sidePotPlayers = new ArrayList<>();
            for (Player p : players) {
                if (p.getAction() != Action.ALLIN || p.getAction() != Action.FOLD) sidePotPlayers.add(p);
            }
            pots.add(new Pot(sidePotPlayers));
            this.betType = BetType.SIDEPOT;
        return true;
        } else if(cond3) {
            this.betType = BetType.ENDROUND;
            return  true;
        } else if(cond4) {
            this.betType = BetType.SKIP2SHOWDOWN;
            return true;
        } else if(cond1) {
            if (numAllIn > 0) this.betType = BetType.SKIP2SHOWDOWN;
            else this.betType = BetType.NORMAL;
            return true;
        } else if(cond2) {
            if (numFold == players.size()-1) this.betType = BetType.SKIP2SHOWDOWN;
            else this.betType = BetType.NORMAL;
            return true;
        }
            return false;
    }

    public void checkBetType() {
        if (roundStatus == RoundStatus.BETTING1) {
            BetTypeLogic.executeBets(BetType.NORMAL);
            betting();
        }
        switch (betType) {
            case NORMAL:
                betting();
            case ENDROUND:
                pots.getFirst().removeFolded(roundStatus);
                pots.getFirst().payOut();
                End();
            case SKIP2SHOWDOWN:
                if (roundStatus == RoundStatus.SHOWDOWN) {
                    betting();
                    for (Pot p : pots) {
                        p.showDown();
                    }
                }
            case SIDEPOT:
                betting();
        }
    }

    public int getOpenPotIndex() {
        if (pots.size() == 1) return 0;
        for (int i = 0; i < pots.size(); i++) {
            if (pots.get(i).getIsOpen())
                return i;
        }
        return -1;
    }

    public String getCommunityCardsAsString() {
        return communityCards.stream()
                .map(Card::toString)
                .collect(Collectors.joining(","));
    }

    public String getRemainingPlayersAsString() {
        return players.stream()
                .map(Player::getName)
                .collect(Collectors.joining(","));
    }
}
