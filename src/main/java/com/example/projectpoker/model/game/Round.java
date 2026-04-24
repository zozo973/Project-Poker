package com.example.projectpoker.model.game;

import com.example.projectpoker.database.DatabaseManager;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.BetType;
import com.example.projectpoker.model.game.enums.RoundStatus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.example.projectpoker.model.game.PotUtil.*;

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
    private final CardDeck deck;
    private ArrayList<Pot> pots;
    private ArrayList<Card> communityCards;
    private ArrayList<RoundLogEntry> roundLog;
    private final ArrayList<Player> players;
    private final ArrayList<Integer> turnOrder;

    private final int gameSessionId;
    private final int roundNumber;
    private boolean persisted;

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
        if (betType.equals(BetType.SKIP2SHOWDOWN) && !roundStatus.equals(RoundStatus.SHOWDOWN)) deal2Table();
    }

    public ArrayList<Pot> getPots() { return pots; }

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
        return pots.get(getOpenPotIndex(pots));
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
        this.roundLog = new ArrayList<>();
        this.pots.getFirst().initBlinds(players, turnOrder, toPlay);
        this.pots.getFirst().setPotPriority(0);
        setRoundStatus(RoundStatus.BLINDS);
    }

    public void start() {
        dealCards();
        setRoundStatus(RoundStatus.DEAL);

//        setRoundStatus(RoundStatus.BETTING1);
//        checkBetType();
//
//        setRoundStatus(RoundStatus.FLOP);
//        deal2Table();
//
//        setRoundStatus(RoundStatus.BETTING2);
//        checkBetType();
//
//        setRoundStatus(RoundStatus.TURN);
//        deal2Table();
//
//        setRoundStatus(RoundStatus.BETTING3);
//        checkBetType();
//
//        setRoundStatus(RoundStatus.RIVER);
//        deal2Table();
//
//        setRoundStatus(RoundStatus.SHOWDOWN);
//        checkBetType();
    }

    public void end() {

        for (Pot p : pots) {
            p.removeFolded(roundStatus);
            p.showDown(this.communityCards);
        }
        // TODO send RoundLog to database exit round
        // DAO.add(getRoundLog());
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
    public void recordPlayerAction(Player player) {
        int betSize = player.getActiveBet();
        Action action = player.getAction();
        if (Action.isBet(action)) {
            this.roundLog.add(new RoundLogEntry(player, toPlay - player.getTotalInvestment(), betSize, action, getOpenPot()));
        } else {
            this.roundLog.add(new RoundLogEntry(player, betSize));
        }
    }

    private void dealCards() {
        for (int repeat = 0; repeat < 2; repeat++) {
            for (Integer integer : turnOrder) {
                players.get(integer).addCardToHand(deck.draw());
            }
        }
    }

    private void deal2Table() {
        if (betType.equals(BetType.SKIP2SHOWDOWN)) {
            switch (communityCards.size()) {
                case 0:
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    this.communityCards.add(deck.draw());
                    this.communityCards.add(deck.draw());
                case 3:
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                case 4:
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                case 5:
                    throw new IllegalStateException("RoundStatus must be showdown, to have 5 community cards.");
                default:
                    throw new IllegalStateException("community cards has illegal amount of cards");
            }
        }
        deck.burnCard();
        if (roundStatus == RoundStatus.FLOP) {
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
        } else {
            this.communityCards.add(deck.draw());
        }
    }

    private void createSidePot(Player potCreator) {
        this.pots = addNewSidePot(this.pots,potCreator);
        this.roundLog.add(new RoundLogEntry(potCreator, pots.getLast()));
    }

    private void betting() {
        Player activePlayer = null;
        do {
            for (Integer i : turnOrder) {
                activePlayer = players.get(i);
                if (!Action.hasFolded(activePlayer.getAction())) {
                    activePlayer.setIsTurn(true);
                    while (activePlayer.getIsTurn()) {
                        // Wait for input on bet or check or fold
                        // call method to asked user for input
                        // TODO Add IU input to make user decision
                        // turnOrder.get(i).setAction(actionButtonInput());
                        activePlayer.chooseBetSize();
                        if (activePlayer.getActiveBet() > this.toPlay && Action.isRaise(activePlayer.getAction())) {
                            this.toPlay = activePlayer.getActiveBet();
                        }
                        if (this.betType.equals(BetType.SIDEPOT)) {
                            betIntoSidePots(activePlayer);
                        } else {
                            setPots(payOpenPot(this.pots, activePlayer));
                        }
                        recordPlayerAction(activePlayer);
                        if (endBetting(activePlayer)) break;
                        if (!activePlayer.getAction().equals(Action.UNDECIDED)) {
                            activePlayer.setIsTurn(false);
                        }
                    }
                }
                if (endBetting(activePlayer)) break;
            }
        } while (endBetting(activePlayer));
        postBetting();
    }

    private void betIntoSidePots(Player activePlayer) {
        var paidPots = tryPayMultiplePots(this.pots,activePlayer);
        if (paidPots != null ) {
            this.pots = paidPots;
        } else {
            setPots(payOpenPot(this.pots,activePlayer));
        }
    }

    private void postBetting() {
        updateTurnOrder();
        for (Player p : players) {
            if (!Action.hasFolded(p.getAction()) || !p.getAction().equals(Action.FORFEIT)) {
                p.setAction(Action.UNDECIDED);
            }
            p.setIsTurn(false);
        }
        if (pots.size()>1) {
            for (int i = 0; i < pots.size()-1; i++) {
                pots.get(i).setIsOpen(false);
            }
        }
        this.toPlay = 0;
    }

    public boolean endBetting(Player activePlayer) {
        if (activePlayer == null) return false;
        if (Action.isRaise(activePlayer.getAction())) return false;
        int numAllIn = 0;
        int numCall = 0;
        int numRaise = 0;
        int numFold = 0;
        int numCheck = 0;
        Player recentAllIn = new Player();
        for (Player p : players) {
            if (p.getAction() == Action.ALLIN) {
                numAllIn += 1;
                recentAllIn = p;
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

        // TODO Test and check betting end conditions possible add more.
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
        // end betting condition 4 (All In - sidePot and end):
        //      Multiple players action is All In,
        //      no players action is a raise,
        //      one or no player has called
        //      All other players actions are fold.
        boolean cond4 = (players.size()-numCall-numAllIn-numFold) == 0 && (numCall == 1 || numCall == 0);
        // sidePot creation condition 1:
        //      One or more players action is all in,
        //      A player who goes after players that are all in has action raise,
        //      All other players actions are call or fold.
        boolean sidePotCond1 = (numAllIn >= 1) && activePlayer.getAction().equals(Action.RAISE) &&
                recentAllIn.getActiveBet() < activePlayer.getActiveBet();

        // sidePot creation condition 2
        //      Previous player raise, current player goes all in
        //      current players all in < the raise
        boolean createSidePot = true;
        for (Pot pot : pots) {
            if (activePlayer.getActiveBet() == pot.getToPlay()) {
                createSidePot = false;
                break;
            }
        }
        boolean sidePotCond2 = activePlayer.getAction().equals(Action.ALLIN) && activePlayer.getActiveBet() < this.toPlay
                && createSidePot;

        if (sidePotCond1 || sidePotCond2) {
            createSidePot(activePlayer);
            setBetType(BetType.SIDEPOT);
            return false;
        } else if(cond3) {
            setBetType(BetType.ENDROUND);
            return  true;
        } else if(cond4) {
            setBetType(BetType.SKIP2SHOWDOWN);
            createSidePot(activePlayer);
            setBetType(BetType.SIDEPOT);
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
                break;
            case BetType.SKIP2SHOWDOWN:
                if (!roundStatus.equals(RoundStatus.SHOWDOWN)) deal2Table();
                betting();
                for (Pot p : pots) {
                    p.showDown(this.communityCards);
                }
                break;
            case BetType.SIDEPOT:
                betting();
                break;
        }
    }

    public void removeForfeited() {
        for (Player p : this.players) {
            if (p.getAction().equals(Action.FORFEIT)) {
                p.forfeitGame();
                players.remove(p);
                this.numPlayers--;
            }
        }
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