package com.example.projectpoker.model.game;
import com.example.projectpoker.model.HandEvaluation;
import com.example.projectpoker.model.PlayerResult;
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
    private boolean holeCardsDealt;

    private final int gameSessionId;
    private final int roundNumber;
    private final boolean persisted;

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
        this.holeCardsDealt = false;
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
        RoundStatus oldVal = this.roundStatus;
        this.roundStatus = roundStatus;
        pcs.firePropertyChange("state", oldVal, this.roundStatus);
    }

    public BetType getBetType() {
        return betType;
    }

    public ArrayList<RoundLogEntry> getRoundLog() { return roundLog; }

    public void setBetType(BetType betType) {
        BetType oldVal = this.betType;
        this.betType = betType;
        pcs.firePropertyChange("betType", oldVal, this.betType);
        if (betType.equals(BetType.ENDROUND)) end();
        if (betType.equals(BetType.SKIP2SHOWDOWN) && !roundStatus.equals(RoundStatus.SHOWDOWN)) deal2Table();
    }

    public ArrayList<Pot> getPots() { return pots; }

    public void setPots(ArrayList<Pot> pots) {
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
        ArrayList<Card> oldVal = this.communityCards;
        this.communityCards = communityCards;
        pcs.firePropertyChange("communityCards", oldVal, this.communityCards);
    }

    public int getToPlay() {
        return toPlay;
    }

    public void setToPlay(int toPlay) {
        int oldVal = this.toPlay;
        this.toPlay = toPlay;
        pcs.firePropertyChange("toPlay", oldVal, this.toPlay);
    }

    public ArrayList<RoundLogEntry> removeRoundLog() {
        return roundLog;
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
        this.pots.getFirst().initBlinds(players, turnOrder, toPlay);
        this.pots.getFirst().setPotPriority(0);
        setToPlay(this.pots.getFirst().getToPlay());
        setPots(new ArrayList<>(this.pots));
        setRoundStatus(RoundStatus.BLINDS);
    }

    public void start() {
        dealCards();
        setRoundStatus(RoundStatus.DEAL);

        setRoundStatus(RoundStatus.BETTING1);
        checkBetType();
    }

    public void end() {

        for (Pot p : pots) {
            RoundStatus statusAfterFoldCheck = p.removeFolded(roundStatus);
            if (roundStatus != RoundStatus.SHOWDOWN && statusAfterFoldCheck != RoundStatus.END) {
                p.showDown(this.communityCards);
            }
        }

        // TODO send RoundLog to database exit round
        // DAO.add(getRoundLog());
        setRoundStatus(RoundStatus.END);
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
            this.roundLog.add(new RoundLogEntry(player, toPlay - player.getTotalInvestment(), betSize, action, getOpenPot()));
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
        Player activePlayer;
        boolean bettingEnded;
        do {
            bettingEnded = false;
            boolean anyPlayerAsked = false;
            for (Integer i : turnOrder) {
                activePlayer = players.get(i);
                if (!Action.hasFolded(activePlayer.getAction()) && activePlayer.getAction() == Action.UNDECIDED) {
                    anyPlayerAsked = true;
                    activePlayer.setIsTurn(true);
                    while (activePlayer.getIsTurn()) {
                        int requiredToCall = getRequiredToCall(activePlayer);
                        int playerBalance = activePlayer.getBalance();

                        // If player is all-in (balance <= 0), they automatically check
                        if (playerBalance <= 0) {
                            activePlayer.setAction(Action.CHECK);
                            activePlayer.setActiveBet(0);
                        }
                        // If player has balance but not enough to call, they automatically go all-in
                        else if (requiredToCall > playerBalance) {
                            activePlayer.setAction(Action.ALLIN);
                            activePlayer.setActiveBet(playerBalance);
                        }
                        else if (activePlayer instanceof AiPlayer) {
                            chooseAiAction(activePlayer);
                        } else {
                            waitForUserAction(activePlayer);
                        }

                        processPlayerAction(activePlayer);
                        if (endBetting(activePlayer)) {
                            bettingEnded = true;
                            break;
                        }
                        if (!activePlayer.getAction().equals(Action.UNDECIDED)) {
                            activePlayer.setIsTurn(false);
                        }
                    }
                }
                if (bettingEnded) {
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

        // If all non-folded players are all-in, betting is complete
        if (numUndecided == 0 && (players.size() - numFold) <= numAllIn + 1) {
            return true;
        }

        // If only one non-folded player remains, betting is complete
        return (players.size() - numFold) == 1;
    }

    private void waitForUserAction(Player activePlayer) {
        while (activePlayer.getIsTurn() && activePlayer.getAction() == Action.UNDECIDED) {
            try {
                // need to pause to wait for the active player's action.
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for user input", e);
            }
        }
    }

    // Temporary AI behaviour AI always checks or calls raises if it can.
    private void chooseAiAction(Player activePlayer) {
        int requiredToCall = getRequiredToCall(activePlayer);

        // If player is all-in (balance = 0), they can only check
        if (activePlayer.getBalance() <= 0) {
            activePlayer.setAction(Action.CHECK);
            activePlayer.setActiveBet(0);
            return;
        }

        if (requiredToCall == 0) {
            activePlayer.setAction(Action.CHECK);
            activePlayer.setActiveBet(0);
            return;
        }

        int betAmount = Math.min(requiredToCall, activePlayer.getBalance());
        if (betAmount < requiredToCall) {
            activePlayer.setAction(Action.ALLIN);
        } else {
            activePlayer.setAction(Action.CALL);
        }
        activePlayer.setActiveBet(betAmount);
    }

    private int getRequiredToCall(Player player) {
        int openPotIndex = getOpenPotIndex(this.pots);
        if (openPotIndex < 0) {
            return Math.max(0, toPlay - player.getTotalInvestment());
        }

        Pot openPot = this.pots.get(openPotIndex);
        int investedInOpenPot = player.getTotalPotInvestment(openPot);
        return Math.max(0, toPlay - investedInOpenPot);
    }

    private void processPlayerAction(Player activePlayer) {
        Action action = activePlayer.getAction();
        Integer activeBet = activePlayer.getActiveBet();
        int requiredToCall = getRequiredToCall(activePlayer);

        // UI raises are interpreted as "raise to" total for this pot.
        // Convert to an incremental contribution so repeat raises do not overcharge the player.
        int betContribution = activeBet == null ? 0 : activeBet;

        // CALL always means paying exactly the current required amount.
        // If nothing is required, normalize to CHECK.
        if (action == Action.CALL) {
            if (requiredToCall <= 0) {
                action = Action.CHECK;
                activePlayer.setAction(action);
                betContribution = 0;
            } else {
                betContribution = requiredToCall;
                activePlayer.setActiveBet(betContribution);
            }
        }

        if (action == Action.RAISE) {
            if (activeBet == null || activeBet <= this.toPlay) {
                action = requiredToCall > 0 ? Action.CALL : Action.CHECK;
                activePlayer.setAction(action);
                betContribution = requiredToCall;
            } else {
                Pot openPot = getOpenPot();
                int alreadyInvestedInOpenPot = activePlayer.getTotalPotInvestment(openPot);
                betContribution = Math.max(0, activeBet - alreadyInvestedInOpenPot);
            }
        }

        // Allow players to act again on raises
        if (action == Action.RAISE  && activeBet > this.toPlay) {
            setToPlay(activeBet);
            resetOtherPlayerActions(activePlayer);
        }

        if (Action.isBet(action)) {
            activeBet = betContribution;
            if (activeBet <= 0) {
                throw new IllegalStateException("Bet action requires a positive active bet.");
            }

            activePlayer.setActiveBet(betContribution);

            if (this.betType.equals(BetType.SIDEPOT)) {
                betIntoSidePots(activePlayer);
            } else {
                setPots(new ArrayList<>(payOpenPot(this.pots, activePlayer)));
            }
        }

        recordPlayerAction(activePlayer);
        // Notify listeners after each completed action (check/call/raise/fold/all-in).
        setPots(new ArrayList<>(this.pots));
    }

    // Allows players to act again in the same round after a raise
    private void resetOtherPlayerActions(Player aggressor) {
        for (Player player : players) {
            if (player == aggressor) {
                continue;
            }
            if (Action.hasFolded(player.getAction()) || player.getAction() == Action.ALLIN) {
                continue;
            }
            player.setAction(Action.UNDECIDED);
        }
    }

    private void betIntoSidePots(Player activePlayer) {
        ArrayList<Pot> paidPots = tryPayMultiplePots(this.pots,activePlayer);
        if (paidPots != null ) {
            setPots(new ArrayList<>(paidPots));
        } else {
            setPots(new ArrayList<>(payOpenPot(this.pots,activePlayer)));
        }
    }

    private void postBetting() {
        updateTurnOrder();
        for (Player p : players) {
            if (!Action.hasFolded(p.getAction()) && !p.getAction().equals(Action.FORFEIT)) {
                p.setAction(Action.UNDECIDED);
            }
            p.setIsTurn(false);
            p.setActiveBet(null);
        }
        if (pots.size()>1) {
            for (int i = 0; i < pots.size()-1; i++) {
                pots.get(i).setIsOpen(false);
            }
        }
        setToPlay(0);
        setPots(new ArrayList<>(this.pots));
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
        BetTypeLogic.executeBets(BetType.NORMAL);
        betting();

        switch (betType) {
            case ENDROUND:
                end();
                return;
            case SKIP2SHOWDOWN:
                playShowdown();
                return;
            case SIDEPOT:
            case NORMAL:
                advanceRound();
                return;
            default:
        }
    }

    private void advanceRound() {
        switch (roundStatus) {
            case BETTING1:
                setRoundStatus(RoundStatus.FLOP);
                deal2Table();
                setRoundStatus(RoundStatus.BETTING2);
                checkBetType();
                break;
            case BETTING2:
                setRoundStatus(RoundStatus.TURN);
                deal2Table();
                setRoundStatus(RoundStatus.BETTING3);
                checkBetType();
                break;
            case BETTING3:
                setRoundStatus(RoundStatus.RIVER);
                deal2Table();
                setRoundStatus(RoundStatus.BETTING4);
                checkBetType();
                break;
            case BETTING4:
                playShowdown();
                break;
            default:
                break;
        }
    }

    private void playShowdown() {
        while (communityCards.size() < 5) {
            deal2Table();
        }
        setRoundStatus(RoundStatus.SHOWDOWN);

        // Remove folded players from pots before showdown evaluation
        for (Pot p : pots) {
            p.removeFolded(roundStatus);
        }

        announceShowdownResults();
        for (Pot p : pots) {
            p.showDown(this.communityCards);
        }
        end();
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