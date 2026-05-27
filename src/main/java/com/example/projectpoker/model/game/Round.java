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
import com.example.projectpoker.AIActions;
import java.util.List;

import static com.example.projectpoker.model.game.PotUtil.*;

public class Round {
    private static final int MAX_BETTING_PASSES = 100;
    private static final long HUMAN_DECISION_POLL_MS = 25L;
    private static final long BETTING_ENTRY_UI_DELAY_MS = 25L;

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
    private volatile boolean stopRequested;


    /** Constructor called when starting a new round of poker
     *
     * @param players: A list of all players participating in the round
     * @param blindSize: Size of the blinds
     * @param gameSessionId: gameSessionId for data Base
     * @param roundNumber: Passed from the game class, to display the round number in the GUI
     */

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
        this.stopRequested = false;
        setRoundStatus(RoundStatus.UNINITIALISED); // Possibly change
    }

    /** Constructor called for unit tests the Round class
     */

    public Round(ArrayList<Player> players, int blindSize) {
        this(players, blindSize, -1, 0);
    }

    /**
     * Registers a listener for all property change events fired by this round
     * (e.g. state, pots, communityCards, toPlay, betType, logEntry).
     *
     * @param listener the {@link PropertyChangeListener} to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a previously registered property change listener from this round.
     *
     * @param listener the {@link PropertyChangeListener} to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Returns the current status of this round (e.g. DEAL, BETTING1, FLOP, SHOWDOWN, END).
     *
     * @return the current {@link RoundStatus}
     */
    public RoundStatus getRoundStatus() {
        return roundStatus;
    }

    /**
     * Transitions the round to a new status, triggering the appropriate game logic
     * (e.g. dealing cards, betting, dealing community cards, or ending the round).
     *
     * @param roundStatus the target {@link RoundStatus} to transition to
     */
    public void setRoundStatus(RoundStatus roundStatus) {
        var oldVal = this.roundStatus;
        this.roundStatus = roundStatus;
        emitLog("Round phase changed from " + oldVal + " to " + roundStatus + ".");
        switch (roundStatus) {
            case DEAL -> {
                dealCards();
                pauseForUiRender();
                pcs.firePropertyChange("state",oldVal,this.roundStatus);
            }
            case BETTING1, BETTING2, BETTING3, BETTING4 -> {
                pcs.firePropertyChange("state",oldVal,this.roundStatus);
                checkBetType();
            }
            case RIVER, TURN, FLOP, SHOWDOWN -> {
                deal2Table();
                pauseForUiRender();
                pcs.firePropertyChange("state",oldVal,this.roundStatus);
                setRoundStatus(RoundStatus.stepRoundStatus(this.roundStatus));
            }
            case END -> {
                pcs.firePropertyChange("state",oldVal,this.roundStatus);
                end();
            }
            case BLINDS -> {
                pcs.firePropertyChange("state",oldVal,this.roundStatus);
                payBlinds();
            }
        }

    }

    /**
     * Returns the current bet type for this round, which affects how the betting phase concludes.
     *
     * @return the current {@link BetType}
     */
    public BetType getBetType() {
        return betType;
    }

    /**
     * Returns the ordered log of actions that have occurred in this round.
     *
     * @return the list of {@link RoundLogEntry} objects, or {@code null} before initialisation
     */
    public ArrayList<RoundLogEntry> getRoundLog() { return roundLog; }

    /**
     * Returns the final snapshot of this round, created at the end of the round.
     *
     * @return the completed {@link RoundLog}, or {@code null} if the round has not ended yet
     */
    public RoundLog getFinalLog() { return this.finalLog; }

    /**
     * Returns whether this round has already been saved to the database.
     *
     * @return {@code true} if the round has been persisted, {@code false} otherwise
     */
    public boolean isPersisted() {
        return persisted;
    }

    /**
     * Marks this round as persisted so it is not saved to the database a second time.
     */
    public void markPersisted() {
        this.persisted = true;
    }

    /**
     * Sets the bet type and fires a "betType" property change event.
     * Setting to ENDROUND triggers {@link #end()}; setting to SKIP2SHOWDOWN fast-forwards community cards.
     *
     * @param betType the new {@link BetType} to assign
     */
    public void setBetType(BetType betType) {
        var oldVal = this.betType;
        this.betType = betType;
        emitLog("Bet type changed from " + oldVal + " to " + this.betType + ".");
        pcs.firePropertyChange("betType", oldVal, this.betType);
        if (betType.equals(BetType.ENDROUND)) end();
        if (betType.equals(BetType.SKIP2SHOWDOWN) && !roundStatus.equals(RoundStatus.SHOWDOWN)) deal2Table();
    }

    /**
     * Returns the list of all pots active in this round (main pot and any side pots).
     *
     * @return the list of {@link Pot} objects
     */
    public ArrayList<Pot> getPots() { return pots; }

    /**
     * Replaces the pots list and fires a "pots" property change event to update the UI.
     *
     * @param pots the new list of {@link Pot} objects
     */
    public void setPots(ArrayList<Pot> pots) {
        this.pots = pots;
        // Pot internals mutate in place, so always emit to keep UI pot label in sync.
        pcs.firePropertyChange("pots", null, this.pots);
    }

    /**
     * Closes the most recently added pot, appends a new pot to the list,
     * and fires a "pots" property change event.
     *
     * @param pot the new {@link Pot} to add to the round
     */
    public void addPot(Pot pot) {
        ArrayList<Pot> pots = getPots();
        pots.getLast().setIsOpen(false);
        pots.add(pot);
        setPots(pots);
    }

    /**
     * Returns the main (first) pot for this round.
     *
     * @return the main {@link Pot}
     */
    public Pot getMainPot() {
        return this.pots.getFirst();
    }

    private Pot getOpenPot() {
        Integer i = getOpenPotIndex(pots);
        if (i == null) {
            return null;
        }
        return pots.get(i);
    }

    /**
     * Returns the open pot for the given player.
     * Falls back to the best available pot if no single open pot exists.
     *
     * @param player the {@link Player} whose pot context is needed
     * @return the relevant open {@link Pot}, or the best fallback pot
     */
    public Pot tryGetOpenPot(Player player) {
        Pot pot = getOpenPot();
        if (pot != null) {
            return pot;
        } else {
            return findBestAvailablePot(this.pots,player);
        }
    }

    /**
     * Returns the list of community cards currently dealt to the board.
     *
     * @return the list of face-up {@link Card} objects on the table
     */
    public ArrayList<Card> getCommunityCards() {
        return communityCards;
    }

    /**
     * Sets the community cards list and fires a "communityCards" property change event.
     *
     * @param communityCards the new list of community {@link Card} objects to display
     */
    public void setCommunityCards(ArrayList<Card> communityCards) {
        var oldVal = this.communityCards;
        this.communityCards = communityCards;
        pcs.firePropertyChange("communityCards", oldVal, this.communityCards);
    }

    /**
     * Returns the current amount a player must call to stay in the hand.
     *
     * @return the to-call amount in chips
     */
    public int getToPlay() {
        return toPlay;
    }

    /**
     * Returns the sum of the highest investment-per-player across all pots.
     * This represents the total chips a new player would need to be fully invested.
     *
     * @return total chips required across all pots
     */
    public int getTotalToPlay() {
        int total = 0;
        for (Pot p : this.pots) {
            total += p.getInvestmentPP();
        }
        return total;
    }

    /**
     * Updates the to-call amount, fires a "toPlay" event, and propagates the value to all pots.
     *
     * @param toPlay the new amount every active player must match
     */
    public void setToPlay(int toPlay) {
        var oldVal = this.toPlay;
        this.toPlay = toPlay;
        pcs.firePropertyChange("toPlay",oldVal,this.toPlay);
        setPotsToPlay(this.pots, toPlay);
    }

    /**
     * Returns and clears the round action log (used when transferring the log out of the round).
     *
     * @return the list of {@link RoundLogEntry} objects recorded so far
     */
    public ArrayList<RoundLogEntry> removeRoundLog() {
        return roundLog;
    }

    private void emitLog(String message) {
        pcs.firePropertyChange("logEntry", null, message);
    }

    private void addRoundLogEntry(RoundLogEntry entry) {
        this.roundLog.add(entry);
        emitLog(entry.getEntryDescription());
    }

    private void pauseForUiRender() {
        try {
            Thread.sleep(BETTING_ENTRY_UI_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void payBlinds() {
        this.pots.getFirst().initBlinds(players, turnOrder, toPlay);
        this.pots.getFirst().setPotPriority(0);
        setToPlay(this.pots.getFirst().getToPlay());
        setPots(new ArrayList<>(this.pots));
    }

    /**
     * Initialises the round: resets all players, builds the turn order from role indices,
     * creates a blank round log, and sets the status to BLINDS.
     */
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

    /**
     * Starts the round by dealing hole cards (DEAL phase) and then beginning pre-flop betting (BETTING1).
     */
    public void start() {
        setRoundStatus(RoundStatus.DEAL);

        setRoundStatus(RoundStatus.BETTING1);
    }

    /**
     * Finalises the round by clearing community cards and building the final {@link RoundLog} snapshot.
     */
    public void end() {
        setCommunityCards(new ArrayList<>());

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

        turnOrder.add(smallBlindIndex);
        if (bigBlindIndex != smallBlindIndex) {
            turnOrder.add(bigBlindIndex);
        }

        int seat = (bigBlindIndex + 1) % numPlayers;
        while (turnOrder.size() < numPlayers) {
            if (!turnOrder.contains(seat)) {
                turnOrder.add(seat);
            }
            seat = (seat + 1) % numPlayers;
        }
    }

    /**
     * Returns the zero-based index of the human (non-AI) player within the players list.
     *
     * @return the user's index, or {@code -1} if no human player is present
     */
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

    /**
     * Records the action taken by the given player into the round log.
     *
     * @param player the {@link Player} whose most recent action should be logged
     */
    public void recordPlayerAction(Player player) {
        Integer activeBetValue = player.getActiveBet();
        int betSize = activeBetValue != null ? activeBetValue : 0;
        Action action = player.getAction();
        if (Action.isBet(action)) {
            addRoundLogEntry(new RoundLogEntry(player, toPlay , betSize, action, tryGetOpenPot(player)));
        } else if (Action.hasFolded(player.getAction())) {
            addRoundLogEntry(new RoundLogEntry(player,player.getName() + " has decided to fold."));
        } else {
            addRoundLogEntry(new RoundLogEntry(player));
        }
    }

    private void dealCards() {
        if (holeCardsDealt) {
            return;
        }

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
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    this.roundStatus = RoundStatus.SHOWDOWN;
                    break;
                case 3:
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    this.roundStatus = RoundStatus.SHOWDOWN;
                    break;
                case 4:
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    this.roundStatus = RoundStatus.SHOWDOWN;
                    break;
                case 5:
                    this.roundStatus = RoundStatus.SHOWDOWN;
                    break;
                default:
                    throw new IllegalStateException("community cards has illegal amount of cards");
            }
            setCommunityCards(this.communityCards);
            return;
        }
        if (roundStatus.equals(RoundStatus.SHOWDOWN)) {
            setCommunityCards(this.communityCards);
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
        setCommunityCards(this.communityCards);
    }

    private void createSidePot(Player potCreator,int newPotSize) {
        this.pots = addNewSidePot(this.pots,potCreator,newPotSize);
        addRoundLogEntry(new RoundLogEntry(potCreator, pots.getLast()));
    }

    private void betting() {
        if (stopRequested) {
            return;
        }
        Player activePlayer;
        int bettingPasses = 0;
        boolean bettingEnded;
        do {
            bettingPasses++;
            if (bettingPasses > MAX_BETTING_PASSES) {
                System.err.println("Betting pass limit reached; auto-resolving remaining undecided players.");
                emitLog("Betting took too long, so remaining undecided players were auto-resolved.");
                resolveStalledBettingRound();
                bettingEnded = true;
                break;
            }
            bettingEnded = false;
            for (Integer i : turnOrder) {
                if (stopRequested) {
                    bettingEnded = true;
                    break;
                }
                activePlayer = players.get(i);
                setToPlay(getToCall(this.pots,activePlayer));
                emitLog(activePlayer.getName() + " to call: " + this.toPlay + ". Current action: " + activePlayer.getAction() + ".");
                if (!Action.hasFolded(activePlayer.getAction()) && activePlayer.getAction().equals(Action.UNDECIDED)) {
                    if (testAllPlayersFolded(activePlayer)) {
                        endBetting(activePlayer);
                        break;
                    }
                    if (testAllPlayersFolded(activePlayer)) {
                        activePlayer.setAction(Action.CHECK);
                        activePlayer.setActiveBet(0);
                        recordPlayerAction(activePlayer);
                        bettingEnded = endBetting(activePlayer);
                        break;
                    }

                    activePlayer.setIsTurn(true);
                    while (activePlayer.getIsTurn() && !stopRequested) {
                        processActivePlayer(activePlayer);

                        if (!activePlayer.getAction().equals(Action.UNDECIDED)) {
                            activePlayer.setIsTurn(false);
                        }
                    }
                    checkIfPlayerRaised(activePlayer);
                    checkCreateNewPot(activePlayer);

                    if (Action.isBet(activePlayer.getAction())) setPots(new ArrayList<>(handlePlayerBet(this.pots,activePlayer)));
                }
                recordPlayerAction(activePlayer);

                if (endBetting(activePlayer)) {
                    bettingEnded = true;
                    break;
                }
            }
        } while (!bettingEnded);
        if (stopRequested || roundStatus == RoundStatus.END) {
            return;
        }
        postBetting();
    }

    private void resolveStalledBettingRound() {
        for (Player player : players) {
            if (player.getAction() == Action.UNDECIDED && !Action.hasFolded(player.getAction())) {
                autoResolvePlayerDecision(player, true);
            }
            player.setIsTurn(false);
        }
    }

    /**
     * Creates a new side pot when the active player has gone all-in for less than the current call amount.
     *
     * @param activePlayer the {@link Player} who triggered the side-pot condition
     */
    public void checkCreateNewPot(Player activePlayer) {
        if (activePlayer.getAction().equals(Action.ALLIN) && activePlayer.getActiveBet() < this.toPlay) {
            int newPotSize = activePlayer.getActiveBet();
            createSidePot(activePlayer,newPotSize);
        }
    }

    /**
     * Resets the actions of all other active players to UNDECIDED when the active player raises,
     * and creates side pots for any all-in players affected by the raise.
     *
     * @param activePlayer the {@link Player} who just raised
     */
    public void checkIfPlayerRaised(Player activePlayer) {
        // need to reset action of other users if active player raises unless
        // a player has folded or are all-in.

        if (Action.isRaise(activePlayer.getAction()) && activePlayer.getActiveBet() > this.toPlay) {
            for (Player p : this.players) {
                if (!(p.equals(activePlayer)) && !Action.hasFolded(p.getAction()) && !p.getAction().equals(Action.ALLIN) ) {
                    p.setAction(Action.UNDECIDED);
                } else if (!(p.equals(activePlayer)) && p.getAction().equals(Action.ALLIN)) {
                    int currentPotSize = p.getTotalPotInvestment(findBestAvailablePot(this.pots,p));
                    int newPotSize = activePlayer.getActiveBet()-currentPotSize;
                    createSidePot(activePlayer,newPotSize);
                    p.setAction(Action.CHECK);
                }
            }

        } else if (activePlayer.getAction().equals(Action.ALLIN) && activePlayer.getActiveBet() == this.toPlay) {
            activePlayer.setAction(Action.CHECK);
        }
    }

    /**
     * Tests whether all players other than the active player have folded.
     *
     * @param activePlayer the {@link Player} currently acting (excluded from the check)
     * @return {@code true} if every other player has folded, {@code false} otherwise
     */
    public boolean testAllPlayersFolded(Player activePlayer) {
        for (Player p : this.players) {
            if (!p.equals(activePlayer) && !Action.hasFolded(p.getAction())) {
                return false;
            }
        }
        return true;
    }

    private void waitForPlayerDecision(Player activePlayer) {
        if (stopRequested) {
            return;
        }
        if (activePlayer instanceof AiPlayer) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while Ai Player is thinking", e);
            }
        } else {
            while (!stopRequested && activePlayer.getIsTurn() && activePlayer.getAction() == Action.UNDECIDED) {
                try {
                    Thread.sleep(HUMAN_DECISION_POLL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for user input", e);
                }
            }
        }
    }

    private void processActivePlayer(Player activePlayer) {
        int playerBalance = activePlayer.getBalance();

        if (playerBalance <= 0) {
            activePlayer.setAction(Action.CHECK);
            activePlayer.setActiveBet(0);
            return;
        }

        if (this.toPlay > playerBalance) {
            activePlayer.setAction(Action.ALLIN);
            activePlayer.setActiveBet(playerBalance);
            return;
        }

        if (activePlayer instanceof AiPlayer aiPlayer) {
            if (roundStatus == RoundStatus.BETTING1) {
                int amountToCall = getToCall(this.pots, aiPlayer);
                if (amountToCall <= 0) {
                    aiPlayer.setAction(Action.CHECK);
                    aiPlayer.setActiveBet(0);
                } else {
                    aiPlayer.setAction(Action.CALL);
                    aiPlayer.setActiveBet(Math.min(amountToCall, aiPlayer.getBalance()));
                }
                return;
            }
            aiPlayer.setResponse(getAiDecision(aiPlayer));
        }
        waitForPlayerDecision(activePlayer);

        activePlayer.play(this.pots);
    }

    private AIActions.AiPlayerResponse getAiDecision(AiPlayer aiPlayer) {
        AIActions.AiPlayerResponse fallback = new AIActions.AiPlayerResponse();
        try {
            List<Card[]> aiHands = new ArrayList<>();
            aiHands.add(aiPlayer.getPlayerHand().getCards().toArray(new Card[0]));
            Card[] board = communityCards.toArray(new Card[0]);
            int requiredToCall = getToCall(this.pots, aiPlayer);
            int potSize = pots.stream().mapToInt(Pot::getPotSize).sum();
            Pot activePot = tryGetOpenPot(aiPlayer);
            int alreadyInvested = activePot == null ? 0 : aiPlayer.getTotalPotInvestment(activePot);

            List<Integer> stackSizes = new ArrayList<>();
            stackSizes.add(aiPlayer.getBalance());

            List<Integer> requiredToCallList = new ArrayList<>();
            requiredToCallList.add(requiredToCall);

            List<Integer> alreadyInvestedList = new ArrayList<>();
            alreadyInvestedList.add(alreadyInvested);

            List<AIActions.AiPlayerResponse> responses =
                    new AIActions().getAllChoices(
                            aiHands,
                            board,
                            roundStatus,
                            requiredToCall,
                            potSize,
                            stackSizes,
                            requiredToCallList,
                            alreadyInvestedList
                    );
            if (responses != null && !responses.isEmpty() && responses.get(0) != null) {
                return responses.get(0);
            }
        } catch (Exception e) {
            System.err.println("Gemini API failed, using AI fallback: " + e.getMessage());
        }
        fallback.errormsg = "No AI response available.";
        return fallback;
    }

    private void autoResolvePlayerDecision(Player activePlayer, boolean becauseRoundStalled) {
        int amountToCall = getToCall(this.pots, activePlayer);
        if (amountToCall <= 0 || activePlayer.getBalance() <= 0) {
            activePlayer.setAction(Action.CHECK);
            activePlayer.setActiveBet(0);
            emitLog(activePlayer.getName() + (becauseRoundStalled
                    ? " was auto-checked because betting stalled."
                    : " took too long to act and was auto-checked."));
        } else {
            activePlayer.setAction(Action.FOLD);
            activePlayer.setActiveBet(0);
            emitLog(activePlayer.getName() + (becauseRoundStalled
                    ? " was auto-folded because betting stalled."
                    : " took too long to act and was auto-folded."));
        }
        activePlayer.setIsTurn(false);
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
        addRoundLogEntry(new RoundLogEntry(this.roundStatus + " round has ended."));
        setToPlay(0);
        setRoundStatus(findHighestPriorityPot(this.pots).removeFolded(this.roundStatus));
    }

    /**
     * Evaluates whether the current state of the betting round satisfies any end condition
     * and, if so, sets the appropriate bet type to conclude the round.
     *
     * @param activePlayer the {@link Player} who last acted; used to exclude RAISE actions from triggering end
     * @return {@code true} if betting should stop, {@code false} if it should continue
     */
    public boolean endBetting(Player activePlayer) {
        if (activePlayer == null) return false;
        if (activePlayer.getAction().equals(Action.RAISE)) return false;

        int numAllIn = 0; int numCall = 0; int numRaise = 0; int numFold = 0; int numCheck = 0; int numUndecided = 0;
        for (Player p : players) {
            switch (p.getAction()) {
                case Action.ALLIN:
                    numAllIn++;
                    break;
                case Action.CALL:
                    numCall++;
                    break;
                case Action.RAISE:
                    numRaise++;
                    break;
                case Action.FOLD:
                    numFold++;
                    break;
                case Action.CHECK:
                    numCheck++;
                    break;
                case Action.UNDECIDED:
                    numUndecided++;
                    break;
            }
        }

        System.out.println("Betting summary: undecided=" + numUndecided
                + ", raised=" + numRaise
                + ", all-in=" + numAllIn
                + ", checked=" + numCheck
                + ", called=" + numCall
                + ", folded=" + numFold + ".");

        // TODO Test and check betting end conditions possibly add more.
        // end betting condition 1 (Normal or All In):
        //      One player action is Raise or All In,
        //      All other players actions are call or fold.
        boolean cond1 = numRaise == 1 && numAllIn == 0 && (players.size() - 1 - numCall - numFold - numCheck) == 0;
        // end betting condition 2 (Normal or End):
        //      All players actions are check, call and fold.
        //      this can relate to first betting round where everyone
        //      either calls or folds against the blind and the blind checks.
        boolean cond2 = (players.size() - numCheck - numFold - numCall) == 0;
        // end betting condition 3 (End):
        //      One player action is not fold, all other players actions are fold.
        boolean cond3 = (players.size() - numFold) == 1;
        // end betting condition 4 (All In - sidePot and end):
        //      One players action is All In, no players action is a raise,
        //      all other players actions are folding or calling.
        boolean cond4 = (players.size()-numCall-numAllIn-numFold) == 0 && numAllIn == 1 &&
                !activePlayer.getAction().equals(Action.ALLIN);

        System.out.println("Betting end conditions: c1=" + cond1 + ", c2=" + cond2 + ", c3=" + cond3 + ", c4=" + cond4 + ".");

        if (cond3) {
            playShowdown();
            setBetType(BetType.ENDROUND);
            return true;
        } else if (numUndecided > 0 || numAllIn > 1 || numRaise > 1) {
            return false;
        } else if(cond4) {
            if (numCall == 1) setBetType(BetType.SKIP2SHOWDOWN);
            else setBetType(BetType.NORMAL);
            return true;
        } else if(cond1) {
            setBetType(BetType.NORMAL);
            return true;
        } else if(cond2) {
            if (numFold == players.size()-1 && numCheck == 1) playShowdown();
            return true;
        } else if (checkIfPlayersCannotBet()) {
            setBetType(BetType.SKIP2SHOWDOWN);
            return true;
        }
        return false;
    }

    private boolean checkIfPlayersCannotBet() {
        for (Player p : this.players) {
            if (!Action.hasFolded(p.getAction()) && p.getBalance() != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Drives one full betting pass through the turn order (called from the round-status handler).
     * After betting finishes, handles ENDROUND and SKIP2SHOWDOWN transitions.
     */
    public void checkBetType() {
        if (stopRequested) {
            return;
        }
        betting();

        switch (betType) {
            case ENDROUND -> end();
            case SKIP2SHOWDOWN -> setRoundStatus(RoundStatus.SHOWDOWN);
            case NORMAL, SIDEPOT -> {
                // betting() already completed this phase once; do not re-enter it here.
            }
        }
    }

    private void playShowdown() {
        for (Pot pot : this.pots) {
            if (this.roundStatus.equals(RoundStatus.SHOWDOWN)) {
                int numWinners = pot.showDown(this.communityCards);

                for (Player p : this.players) {
                    if (p.getRole().equals(Roles.WINNER)) {
                        String proNoun = "you";
                        if (p instanceof AiPlayer) proNoun = "they";
                        addRoundLogEntry(new RoundLogEntry(p,p.getName() + " is a winner, "
                                + proNoun + " won " + Math.floor((double) pot.getPotSize()/numWinners) + "."));
                    }
                }
            }
        }
        announceShowdownResults();

        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while displaying Winner.", e);
        }

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

                emitLog(
                        "Showdown: Pot " + potNumber + " winner " + winnerName
                                + " wins " + potShare + " with " + winner.getResult()
                );
            }
            potNumber++;
        }
    }

    /**
     * Removes all players whose action is FORFEIT from the round's player list
     * and updates the internal player count and turn order.
     */
    public void removeForfeited() {
        int oldSize = players.size();
        players.removeIf(p -> p.getAction().equals(Action.FORFEIT));
        this.numPlayers = players.size();
        if (oldSize != this.numPlayers) {
            updateTurnOrder();
        }
    }

    /**
     * Returns the sequential number of this round within the current game session (1-based).
     *
     * @return the round number
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * Returns the community cards formatted as a comma-separated string of card descriptions.
     *
     * @return comma-separated card string, e.g. {@code "Ace of Hearts,King of Spades,Two of Clubs"}
     */
    public String getCommunityCardsAsString() {
        return communityCards.stream()
                .map(Card::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * Returns the names of all remaining players formatted as a comma-separated string.
     *
     * @return comma-separated player name string
     */
    public String getRemainingPlayersAsString() {
        return players.stream()
                .map(Player::getName)
                .collect(Collectors.joining(","));
    }

    /**
     * Signals the round to stop as quickly as possible (e.g. when the game window is closed).
     * Sets all undecided players to FOLD and clears all active-turn flags.
     */
    public void requestStop() {
        this.stopRequested = true;
        for (Player p : players) {
            p.setIsTurn(false);
            if (p.getAction() == Action.UNDECIDED) {
                p.setAction(Action.FOLD);
            }
            p.setActiveBet(0);
        }
    }
}
