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

    public BetType getBetType() {
        return betType;
    }

    public ArrayList<RoundLogEntry> getRoundLog() { return roundLog; }

    public RoundLog getFinalLog() { return this.finalLog; }

    public void setBetType(BetType betType) {
        var oldVal = this.betType;
        this.betType = betType;
        emitLog("Bet type changed from " + oldVal + " to " + this.betType + ".");
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

    private Pot getOpenPot() {
        Integer i = getOpenPotIndex(pots);
        if (i == null) {
            return null;
        }
        return pots.get(i);
    }

    public Pot tryGetOpenPot(Player player) {
        Pot pot = getOpenPot();
        if (pot != null) {
            return pot;
        } else {
            return findBestAvailablePot(this.pots,player);
        }
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

    public int getTotalToPlay() {
        int total = 0;
        for (Pot p : this.pots) {
            total += p.getInvestmentPP();
        }
        return total;
    }

    public void setToPlay(int toPlay) {
        var oldVal = this.toPlay;
        this.toPlay = toPlay;
        pcs.firePropertyChange("toPlay",oldVal,this.toPlay);
        setPotsToPlay(this.pots, toPlay);
    }

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
            addRoundLogEntry(new RoundLogEntry(player, toPlay , betSize, action, tryGetOpenPot(player)));
        } else if (Action.hasFolded(player.getAction())) {
            addRoundLogEntry(new RoundLogEntry(player,player.getName() + " has decided to fold."));
        } else {
            addRoundLogEntry(new RoundLogEntry(player, betSize));
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
                    break;
                case 3:
                    // Turn
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    // River
                    deck.burnCard();
                    this.communityCards.add(deck.draw());
                    this.roundStatus = RoundStatus.SHOWDOWN;
                    break;
                case 4:
                    // River
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
                activePlayer = players.get(i);
                // Check if Action.Undecided is right.
                setToPlay(getToCall(this.pots,activePlayer));
                emitLog(activePlayer.getName() + " to call: " + this.toPlay + ". Current action: " + activePlayer.getAction() + ".");
                if (!Action.hasFolded(activePlayer.getAction()) && activePlayer.getAction().equals(Action.UNDECIDED)) {
                    if (testAllPlayersFolded(activePlayer)) {
                        activePlayer.setAction(Action.CHECK);
                        activePlayer.setActiveBet(0);
                        recordPlayerAction(activePlayer);
                        bettingEnded = endBetting(activePlayer);
                        break;
                    }

                    activePlayer.setIsTurn(true);
                    while (activePlayer.getIsTurn()) {
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
        if (roundStatus == RoundStatus.END) {
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

    public void  checkCreateNewPot(Player activePlayer) {
        if (activePlayer.getAction().equals(Action.ALLIN) && activePlayer.getActiveBet() < this.toPlay) {
            int newPotSize = activePlayer.getActiveBet();
            createSidePot(activePlayer,newPotSize);
        }
    }


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

    public boolean testAllPlayersFolded(Player activePlayer) {
        for (Player p : this.players) {
            if (!p.equals(activePlayer) && !Action.hasFolded(p.getAction())) {
                return false;
            }
        }
        return true;
    }

    private void waitForPlayerDecision(Player activePlayer) {
        if (activePlayer instanceof AiPlayer) {
            //Random random = new Random();
            //int thinkTime = random.nextInt((2000-500)+1) + 500;
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while Ai Player is thinking", e);
            }
        } else {
            while (activePlayer.getIsTurn() && activePlayer.getAction() == Action.UNDECIDED) {
                try {
                    // need to pause to wait for the active player's action.
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

        if (activePlayer instanceof AiPlayer aiPlayer) {
            aiPlayer.setResponse(getAiDecision(aiPlayer));
        }

        waitForPlayerDecision(activePlayer);
        activePlayer.play(this.pots);
        if (activePlayer.getAction() == Action.UNDECIDED) {
            autoResolvePlayerDecision(activePlayer, false);
        }
    }

    private AIActions.AiPlayerResponse getAiDecision(AiPlayer aiPlayer) {
        List<Card[]> aiHands = new ArrayList<>();
        aiHands.add(aiPlayer.getPlayerHand().getCards().toArray(new Card[0]));
        Card[] board = communityCards.toArray(new Card[0]);

        try {
            List<AIActions.AiPlayerResponse> responses =
                    new AIActions().getAllChoices(aiHands, board, roundStatus);
            if (responses != null && !responses.isEmpty() && responses.getFirst() != null) {
                return responses.getFirst();
            }
        } catch (Exception e) {
            System.err.println("Gemini API failed, using AI fallback: " + e.getMessage());
        }

        AIActions.AiPlayerResponse fallback = new AIActions.AiPlayerResponse();
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

        if (numUndecided > 0 || numAllIn > 1 || numRaise > 1) {
            return false;
        } else if (cond3) {
            playShowdown();
            return  true;
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

    public void checkBetType() {
        BetTypeLogic.executeBets(BetType.NORMAL);
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
        // TODO: Reveal players Hands to the user
        //      - if showdown is invoked by having only one player left then do not reveal opponents cards

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