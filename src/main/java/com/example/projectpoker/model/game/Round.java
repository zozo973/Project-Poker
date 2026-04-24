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

    // Round Events
    //      roundStatus Change
    //      pots Change
    //      communityCards Change
    //      toPlay Change
    //      betType Change

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private RoundStatus roundStatus;
    private int numPlayers;
    private int toPlay;
    private BetType betType;
    private CardDeck deck;
    private ArrayList<Pot> pots;
    private ArrayList<Card> communityCards;
    private ArrayList<RoundLogEntry> roundLog;
    private ArrayList<Player> players;
    private ArrayList<Integer> turnOrder;
    private final int gameSessionId;
    private final int roundNumber;
    private boolean persisted;

    public Round(ArrayList<Player> players, int blindSize) {
        this(players, blindSize, -1, 0);
    }

    public Round(ArrayList<Player> players, int blindSize, int gameSessionId, int roundNumber) {
        this.roundStatus = RoundStatus.UNINITIALISED;
        this.players = players;
        this.toPlay = blindSize;
        this.numPlayers = players.size();
        this.communityCards = new ArrayList<>();
        this.deck = new CardDeck();
        this.pots = new ArrayList<>();
        this.pots.add(new Pot(players));
        this.turnOrder = new ArrayList<>();
        this.betType = BetType.NORMAL;
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
        pcs.firePropertyChange("state", oldVal, this.roundStatus);
    }

    public BetType getBetType() {
        return betType;
    }

    public ArrayList<RoundLogEntry> getRoundLog() { return roundLog; }

    public void setBetType(BetType betType) {
        var oldVal = this.betType;
        this.betType = betType;
        pcs.firePropertyChange("betType", oldVal, this.betType);
        if (betType.equals(BetType.ENDROUND)) end();
    }

    public ArrayList<Pot> getPots() {
        return pots;
    }

    public void setPots(ArrayList<Pot> pots) {
        var oldVal = this.pots;
        this.pots = pots;
        pcs.firePropertyChange("pots", oldVal, this.pots);
    }

    public void addPot(Pot pot) {
        ArrayList<Pot> pots = getPots();
        pots.getLast().setIsOpen(false);
        pots.add(pot);
        setPots(pots);
    }

    public Pot getMainPot() {
        return this.pots.getFirst();
    }

    public Pot getOpenPot() {
        return pots.get(getOpenPotIndex());
    }

    public ArrayList<Card> getCommunityCards() {
        return communityCards;
    }

    public void setCommunityCards(ArrayList<Card> communityCards) {
        var oldVal = this.communityCards;
        this.communityCards = communityCards;
        pcs.firePropertyChange("communityCards", oldVal, this.communityCards);
    }

    public int getToPlay() {
        return toPlay;
    }

    public void setToPlay(int toPlay) {
        var oldVal = this.toPlay;
        this.toPlay = toPlay;
        pcs.firePropertyChange("toPlay", oldVal, this.toPlay);
    }

    public ArrayList<RoundLogEntry> removeRoundLog() {
        return roundLog;
    }

    public void init() {
        createTurnOrder(RoleUtil.findRoleIndices(players));

        // add observer to relay state changes to ui addObserver();

        this.pots.getFirst().initBlinds(players, turnOrder, toPlay);
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

    public void end() {

        for (Pot p : pots) {
            p.removeFolded(roundStatus);
            p.payOut();
        }
        if (!persisted) {
            DatabaseManager.recordRound(gameSessionId, this);
            persisted = true;
        }
        setRoundStatus(RoundStatus.END);
    }

    private void createTurnOrder(int[] roleIndices) {
        turnOrder.add((roleIndices[1]));
        for (int i = roleIndices[2]; i < numPlayers; i++) {
            turnOrder.add(i);
        }
        for (int i = 0; i <= roleIndices[0]; i++) {
            turnOrder.add(i);
        }
    }

    public int getUserIndex() {
        for (int i = 0; i < players.size();i++) {
            if (!(players.get(i) instanceof AiPlayer)) return i;
        }
        return -1;
    }

    private void updateTurnOrder() {
        for (Player p : players) {
            if (Action.hasFolded(p.getAction()))
                for (int i : turnOrder) {
                    if (players.get(turnOrder.get(i)).equals(p))
                        this.turnOrder.remove(i);
                }
        }
    }

    // TODO Attach to the game controller
    // On click method depending on player choice
    public void playerAction(Player player, int betSize) {
        if (this.roundLog == null) {
            this.roundLog = new ArrayList<>();
        }
        Action action = player.getAction();
        if (Action.isBet(action)) {
            this.roundLog.add(new RoundLogEntry(player, toPlay - player.getRoundInvestment(), betSize, action, getOpenPot()));
        } else {
            this.roundLog.add(new RoundLogEntry(player, betSize));
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
        if (roundStatus == RoundStatus.FLOP) {
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
        } else {
            this.communityCards.add(deck.draw());
        }
    }

    private void createSidePot() {
        ArrayList<Player> sidePotPlayers = new ArrayList<>();
        for (Player p : players) {
            if (!(p.getAction().equals(Action.ALLIN)) || !(p.getAction().equals(Action.FOLD))) sidePotPlayers.add(p);
        }
        addPot(new Pot(sidePotPlayers));
    }

    private void betting() {
        while (!endBetting()) {
            for (int i = 0; i < turnOrder.size(); i++) {
                Player activePlayer = players.get(turnOrder.get(i));
                if (activePlayer.getAction() != Action.FOLD) {
                    activePlayer.setIsTurn(true);
                    while (activePlayer.getIsTurn()) {
                        // Wait for input on bet or check or fold
                        // call method to asked user for input
                        // turnOrder.get(i).setAction(actionButtonInput());
                        int betSize = activePlayer.chooseBetSize();
                        int activePotIndex = getOpenPotIndex();
                        if (activePotIndex == -1) System.err.println("No active Pot"); // Replace with exeception.

                        this.pots.get(activePotIndex).addBet(activePlayer, betSize);
                        playerAction(activePlayer, betSize);
                        if (Action.isRaise(activePlayer.getAction())) {
                            setToPlay(activePlayer.getRoundInvestment());
                        }
                    }
                }
                if (endBetting()) break;
            }
        }
        updateTurnOrder();
        for (Player p : players) {
            if (!Action.hasFolded(p.getAction())) p.setAction(Action.UNDECIDED);
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
            createSidePot();
            setBetType(BetType.SIDEPOT);
        return true;
        } else if(cond3) {
            setBetType(BetType.ENDROUND);
            return  true;
        } else if(cond4) {
            setBetType(BetType.SKIP2SHOWDOWN);
            return true;
        } else if(cond1) {
            if (numAllIn > 0) setBetType(BetType.SKIP2SHOWDOWN);
            else setBetType(BetType.NORMAL);
            return true;
        } else if(cond2) {
            if (numFold == players.size()-1) setBetType(BetType.SKIP2SHOWDOWN);
            else setBetType(BetType.NORMAL);
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
            case BetType.NORMAL:
                betting();
            case BetType.SKIP2SHOWDOWN:
                if (roundStatus == RoundStatus.SHOWDOWN) {
                    betting();
                    for (Pot p : pots) {
                        p.showDown(this.communityCards);
                    }
                }
            case BetType.SIDEPOT:
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

    public int getRoundNumber() {
        return roundNumber;
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
