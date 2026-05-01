package com.example.projectpoker.model.game;

import java.util.Random;
import com.example.projectpoker.model.HandEvaluation;
import com.example.projectpoker.model.PlayerResult;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.BetType;
import com.example.projectpoker.model.game.enums.Roles;
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
    private RoundLog finalLog;
    private final ArrayList<Player> players;
    private final ArrayList<Integer> turnOrder;
    private boolean holeCardsDealt;

    private final int gameSessionId;
    private final int roundNumber;
    private boolean persisted;

    // Constructor called for unit tests the Round class

    public Round(ArrayList<Player> players, int blindSize) {
        this(players, blindSize, -1, 0);
    }

    /** Constructor called when starting a new round of poker
     * @Params
     *      players: A list of all players participating in the round
     *      blindSize: Size of the blinds
     *      gameSessionId: gameSessionId for data Base
     *      roundNumber: Passed from the game class, to display the round number in the GUI
     */

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
        this.holeCardsDealt = false;
        this.gameSessionId = gameSessionId;
        this.roundNumber = roundNumber;
        this.persisted = false;
        setRoundStatus(RoundStatus.UNINITIALISED); // Possibly change
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
        this.roundStatus = roundStatus;
        switch (roundStatus) {
            case BETTING1, BETTING2, BETTING3, BETTING4 -> checkBetType();
            case RIVER, TURN, FLOP, SHOWDOWN -> deal2Table();
            case DEAL -> dealCards();
            case END -> end();
            case UNINITIALISED -> init();
            case BLINDS -> payBlinds();
        }
    }

    public BetType getBetType() {
        return betType;
    }

    public ArrayList<RoundLogEntry> getRoundLog() { return roundLog; }

    public RoundLog getFinalLog() { return this.finalLog; }

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
        // Pot internals mutate in place, so always emit to keep UI pot label in sync.
        pcs.firePropertyChange("pots", null, this.pots);
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

        this.toPlay = toPlay;
        this.pots = setPotsToPlay(this.pots, toPlay);
    }

    public ArrayList<RoundLogEntry> removeRoundLog() {
        return roundLog;
    }

    private void payBlinds() {
        this.pots.getFirst().initBlinds(players, turnOrder, toPlay);
        this.pots.getFirst().setPotPriority(0);
        setToPlay(this.pots.getFirst().getToPlay());
        setPots(new ArrayList<>(this.pots));
    }

    public void init() {
        // Reset all players' state for the new round
        for (Player player : players) {
            player.roundReset();
        }

        createTurnOrder(RoleUtil.findRoleIndices(players));
        this.holeCardsDealt = false;

        // add observer to relay state changes to ui addObserver();
        this.roundLog = new ArrayList<>();
        setRoundStatus(RoundStatus.BLINDS);
    }

    public void start() {
        setRoundStatus(RoundStatus.DEAL);

        setRoundStatus(RoundStatus.BETTING1);
    }

    public void end() {
        this.finalLog = new RoundLog(
                this.roundLog,
                this.players,
                this.communityCards,
                this.pots,
                this.roundNumber
        );
    }

    private void createTurnOrder(int[] roleIndices) {
        turnOrder.clear();

        int smallBlindIndex = roleIndices[1];
        int bigBlindIndex = roleIndices[2];

        // Keep blind initialization contract: turnOrder[0] = small blind, turnOrder[1] = big blind.
        turnOrder.add(smallBlindIndex);
        if (bigBlindIndex != smallBlindIndex) {
            turnOrder.add(bigBlindIndex);
        }

        // Fill remaining seats clockwise from the player after big blind, once each.
        int seat = (bigBlindIndex + 1) % numPlayers;
        while (turnOrder.size() < numPlayers) {
            if (!turnOrder.contains(seat)) {
                turnOrder.add(seat);
            }
            seat = (seat + 1) % numPlayers;
        }
    }

    public int getUserIndex() {
        for (int i = 0; i < players.size();i++) {
            if (!(players.get(i) instanceof AiPlayer)) return i;
        }
        return -1;
    }

    private void updateTurnOrder() {
        for (int i = turnOrder.size() - 1; i >= 0; i--) {
            Player playerInSeat = players.get(turnOrder.get(i));
            Action action = playerInSeat.getAction();
            if (Action.hasFolded(action) || action == Action.FORFEIT) {
                turnOrder.remove(i);
            }
        }
    }

    // TODO Attach to the game controller
    // On click method depending on player choice
    public void recordPlayerAction(Player player) {
        Integer activeBetValue = player.getActiveBet();
        int betSize = activeBetValue != null ? activeBetValue : 0;
        Action action = player.getAction();
        if (Action.isBet(action)) {
            this.roundLog.add(new RoundLogEntry(player, toPlay , betSize, action, getOpenPot()));
        } else if (Action.hasFolded(player.getAction())) {
            this.roundLog.add(new RoundLogEntry(player,player.getName() + " has decided to fold."));
        } else {
            this.roundLog.add(new RoundLogEntry(player, betSize));
        }
    }

    private void dealCards() {
        if (holeCardsDealt) {
            return;
        }

        // Defensive reset: each round should start with exactly two hole cards per player.
        for (Integer seat : turnOrder) {
            players.get(seat).getPlayerHand().clear();
        }

        for (int repeat = 0; repeat < 2; repeat++) {
            for (Integer integer : turnOrder) {
                players.get(integer).addCardToHand(deck.draw());
            }
        }
        holeCardsDealt = true;
    }

    private void deal2Table() {
        if (betType.equals(BetType.SKIP2SHOWDOWN)) {
            switch (communityCards.size()) {
                case 0:
                    // Flop
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    this.communityCards.add(deck.draw());
                    this.communityCards.add(deck.draw());
                    // Turn
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    // River
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    this.roundStatus = RoundStatus.SHOWDOWN;
                case 3:
                    // Turn
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    // River
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    this.roundStatus = RoundStatus.SHOWDOWN;
                case 4:
                    // River
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    this.roundStatus = RoundStatus.SHOWDOWN;
                case 5:
                    this.roundStatus = RoundStatus.SHOWDOWN;
                default:
                    throw new IllegalStateException("community cards has illegal amount of cards");
            }

        }
        if (roundStatus.equals(RoundStatus.SHOWDOWN)) {
            playShowdown();
            return;
        }

        deck.burnCard();
        if (roundStatus.equals(RoundStatus.FLOP)) {
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
            this.communityCards.add(deck.draw());
        } else {
            this.communityCards.add(deck.draw());
        }
        setRoundStatus(RoundStatus.stepRoundStatus(this.roundStatus));
    }

    private void createSidePot(Player potCreator) {
        this.pots = addNewSidePot(this.pots,potCreator);
        this.roundLog.add(new RoundLogEntry(potCreator, pots.getLast()));
    }

    private void betting() {
        Player activePlayer;
        boolean bettingEnded;
        do {
            bettingEnded = false;
            boolean anyPlayerAsked = false;
            for (Integer i : turnOrder) {
                activePlayer = players.get(i);
                // Check if Action.Undecided is right.
                if (!Action.hasFolded(activePlayer.getAction()) && activePlayer.getAction() == Action.UNDECIDED) {
                    anyPlayerAsked = true;
                    setToPlay(getToCall(this.pots,activePlayer));
                    activePlayer.setIsTurn(true);
                    while (activePlayer.getIsTurn()) {
                        processActivePlayer(activePlayer);

                        if (!activePlayer.getAction().equals(Action.UNDECIDED)) {
                            activePlayer.setIsTurn(false);
                        }
                    }
                }
                recordPlayerAction(activePlayer);

                checkIfPlayerRaised(activePlayer);

                if (endBetting(activePlayer)) {
                    bettingEnded = true;
                    break;
                }
            }
            // If we went through all players without asking any to act, check if betting should end
            if (!anyPlayerAsked) {
                // All remaining players have made their decisions, check if betting is complete
                bettingEnded = checkIfBettingComplete();
            }
        } while (!bettingEnded);
        postBetting();
    }

    private void checkIfPlayerRaised(Player activePlayer) {
        // need to reset action of other users if active player raises unless
        // a player has folded or are all-in.

        if (Action.isRaise(activePlayer.getAction()) && activePlayer.getActiveBet() > this.toPlay) {
            for (Player p : this.players) {
                if (!(p.equals(activePlayer)) || Action.hasFolded(p.getAction()) || p.getAction().equals(Action.ALLIN) ) {
                    p.setAction(Action.UNDECIDED);
                }
            }
        }
    }

    private boolean checkIfBettingComplete() {
        int numAllIn = 0;
        int numCall = 0;
        int numRaise = 0;
        int numFold = 0;
        int numCheck = 0;
        int numUndecided = 0;

        for (Player p : players) {
            if (p.getAction() == Action.ALLIN) {
                numAllIn++;
            } else if (p.getAction() == Action.CALL) {
                numCall++;
            } else if (p.getAction() == Action.RAISE) {
                numRaise++;
            } else if (p.getAction() == Action.FOLD) {
                numFold++;
            } else if (p.getAction() == Action.CHECK) {
                numCheck++;
            } else if (p.getAction() == Action.UNDECIDED) {
                numUndecided++;
            }
        }
        if (numUndecided > 0) return false;

        // If all non-folded players are all-in, betting is complete
        if (numUndecided == 0 && (players.size() - numFold) <= numAllIn + 1) {
            return true;
        }

        // If only one non-folded player remains, betting is complete
        return (players.size() - numFold) == 1;
    }

    private void waitForPlayerDecision(Player activePlayer) {
        if (activePlayer instanceof AiPlayer) {
            Random random = new Random();
            int thinkTime = random.nextInt((4000-500)+1) + 500;
            try {
                Thread.sleep(thinkTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while Ai Player is thinking", e);
            }
        } else {
            while (activePlayer.getIsTurn() && activePlayer.getAction() == Action.UNDECIDED) {
                try {
                    // need to pause to wait for the active player's action.
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for user input", e);
                }
            }
        }
    }

    private void processActivePlayer(Player activePlayer) {
        int playerBalance = activePlayer.getBalance();

        // If player is all-in (balance <= 0), they automatically check
        if (playerBalance <= 0) {
            activePlayer.setAction(Action.CHECK);
            activePlayer.setActiveBet(0);
            return;
        }

        // If player has balance but not enough to call, they automatically go all-in
        else if (this.toPlay > playerBalance) {
            // TODO: Ask all in or fold
            activePlayer.setAction(Action.ALLIN);
            activePlayer.setActiveBet(playerBalance);
        }

        waitForPlayerDecision(activePlayer);
        activePlayer.play(this.pots);
        setPots(handlePlayerBet(this.pots,activePlayer));
    }

    private void postBetting() {
        updateTurnOrder();
        for (Player p : players) {
            if (!Action.hasFolded(p.getAction()) && !p.getAction().equals(Action.FORFEIT)
                    && !p.getAction().equals(Action.ALLIN)) {
                p.setAction(Action.UNDECIDED);
            }
            p.setIsTurn(false);
            p.setActiveBet(null);
        }
        if (pots.size()>1) {
            for (int i = 0; i < pots.size()-1; i++) {
                pots.get(i).setIsOpen(false);
                pots.get(i).removeFolded(roundStatus);
            }
        }
        setToPlay(0);
        setRoundStatus(this.pots.getLast().removeFolded(this.roundStatus));
    }

    public boolean endBetting(Player activePlayer) {
        if (activePlayer == null) return false;
        if (Action.isRaise(activePlayer.getAction())) return false;
        int numAllIn = 0;
        int numCall = 0;
        int numRaise = 0;
        int numFold = 0;
        int numCheck = 0;
        int numUndecided = 0;
        Player recentAllIn = new Player();
        for (Player p : players) {
            switch (p.getAction()) {
                case Action.ALLIN:
                    numAllIn++;
                    recentAllIn = p;
                case Action.CALL:
                    numCall++;
                case Action.RAISE:
                    numRaise++;
                case Action.FOLD:
                    numFold++;
                case Action.CHECK:
                    numCheck++;
                case Action.UNDECIDED:
                    numUndecided++;
            }
        }
        // TODO Test and check betting end conditions possible add more.
        // end betting condition 1 (Normal or All In):
        //      One player action is Raise or All In,
        //      All other players actions are call or fold.
        boolean cond1 = ((numRaise == 1 && numAllIn == 0) || (numRaise == 0 && numAllIn == 1))
                && (players.size() - 1 - numCall - numFold) == 0;
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
         Integer recentAllInBet = recentAllIn.getActiveBet();
         Integer activePlayerBet = activePlayer.getActiveBet();
         boolean sidePotCond1 = (numAllIn >= 1) && activePlayer.getAction().equals(Action.RAISE) &&
                 recentAllInBet != null && activePlayerBet != null && recentAllInBet < activePlayerBet;

         // sidePot creation condition 2
         //      Previous player raise, current player goes all in
         //      current players all in < the raise
         boolean createSidePot = true;
         for (Pot pot : pots) {
             if (activePlayerBet != null && activePlayerBet == pot.getToPlay()) {
                 createSidePot = false;
                 break;
             }
         }
         boolean sidePotCond2 = activePlayer.getAction().equals(Action.ALLIN) && activePlayerBet != null && activePlayerBet < this.toPlay
                 && createSidePot;

        if (sidePotCond1 || sidePotCond2) {
            createSidePot(activePlayer);
            setBetType(BetType.SIDEPOT);
            return false;
        } else if (numUndecided > 0) {
            return false;
        }
        else if(cond3) {
            setBetType(BetType.ENDROUND);
            return  true;
        } else if(cond4) {
            setBetType(BetType.SKIP2SHOWDOWN);
            createSidePot(activePlayer);
            setBetType(BetType.SIDEPOT);
            return true;
        } else if(cond1) {
            if (numAllIn > 0) setBetType(BetType.SKIP2SHOWDOWN);
//            else setBetType(BetType.NORMAL);
            return true;
        } else if(cond2) {
            if (numFold == players.size()-1) setBetType(BetType.SKIP2SHOWDOWN);
//            else setBetType(BetType.NORMAL);
            return true;
        }
        return false;
    }

    public void checkBetType() {
        BetTypeLogic.executeBets(BetType.NORMAL);
        betting();

        switch (betType) {
            case ENDROUND:
                end();
            case SKIP2SHOWDOWN:
                setRoundStatus(RoundStatus.SHOWDOWN);
            case NORMAL, SIDEPOT:
                betting();
        }
    }


    private void playShowdown() {
        while (communityCards.size() < 5) {
            deal2Table();
        }
        // TODO: Reveal players Hands to the user

        for (Pot pot : this.pots) {
            RoundStatus statusAfterFoldCheck = pot.removeFolded(this.roundStatus);
            if (statusAfterFoldCheck == RoundStatus.SHOWDOWN) {
                int numWinners = pot.showDown(this.communityCards);

                for (Player p : this.players) {
                    if (p.getRole().equals(Roles.WINNER)) {
                        String proNoun = "you";
                        if (p instanceof AiPlayer) proNoun = "they";
                        this.roundLog.add(new RoundLogEntry(p,p.getName() + " is a winner, "
                                + proNoun + " won " + Math.floor((double) pot.getPotSize()/numWinners) + "."));
                    }
                }
            }
        }
        announceShowdownResults();
        setRoundStatus(RoundStatus.END);
    }

    private void announceShowdownResults() {
        int potNumber = 1;
        for (Pot pot : pots) {
            ArrayList<Player> eligiblePlayers = pot.getPlayers();
            if (eligiblePlayers == null || eligiblePlayers.isEmpty()) {
                potNumber++;
                continue;
            }

            ArrayList<PlayerResult> winners = HandEvaluation.whoWins(this.communityCards, eligiblePlayers);
            if (winners == null || winners.isEmpty()) {
                potNumber++;
                continue;
            }

            int potShare = winners.size() > 0 ? pot.getPotSize() / winners.size() : 0;
            for (PlayerResult winner : winners) {
                String winnerName = "Unknown";
                for (Player player : eligiblePlayers) {
                    if (player.matchId(winner.getPlayerId())) {
                        winnerName = player.getName();
                        break;
                    }
                }

                System.out.println(
                        "Showdown: Pot " + potNumber + " winner " + winnerName
                                + " wins " + potShare + " with " + winner.getResult()
                );
            }
            potNumber++;
        }
    }

    public void removeForfeited() {
        int oldSize = players.size();
        players.removeIf(p -> p.getAction().equals(Action.FORFEIT));
        this.numPlayers = players.size();
        if (oldSize != this.numPlayers) {
            updateTurnOrder();
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