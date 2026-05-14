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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<Player, AIActions.AiPlayerResponse> aiDecisions = new HashMap<>();

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
        System.out.println("Changing round status from " + oldVal + " to " + roundStatus);
        switch (roundStatus) {
            case DEAL -> {
                dealCards();
                pcs.firePropertyChange("state",oldVal,this.roundStatus);
            }
            case BETTING1, BETTING2, BETTING3, BETTING4 -> {
                pcs.firePropertyChange("state",oldVal,this.roundStatus);
                checkBetType();
            }
            case RIVER, TURN, FLOP, SHOWDOWN -> {
                deal2Table();
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
        System.out.println("BetType changing from" + oldVal + " to " + "betType.");
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
            this.roundLog.add(new RoundLogEntry(player, toPlay , betSize, action, tryGetOpenPot(player)));
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
        this.roundLog.add(new RoundLogEntry(potCreator, pots.getLast()));
    }

    private void betting() {

        // Try to use Gemini first
        aiDecisions = new HashMap<>();
        List<AiPlayer> aiPlayerList = new ArrayList<>();
        List<Card[]> aiHands = new ArrayList<>();
        for (Integer i : turnOrder) {
            Player p = players.get(i);
            if (p instanceof AiPlayer && !Action.hasFolded(p.getAction())) {
                aiPlayerList.add((AiPlayer) p);
                aiHands.add(p.getPlayerHand().getCards().toArray(new Card[0]));
            }
        }
        if (!aiPlayerList.isEmpty()) {
            try {
                Card[] board = communityCards.toArray(new Card[0]);
                List<AIActions.AiPlayerResponse> responses =
                        new AIActions().getAllChoices(aiHands, board, roundStatus);
                for (int i = 0; i < aiPlayerList.size(); i++) {
                    aiDecisions.put(aiPlayerList.get(i), responses.get(i));
                }
            } catch (Exception e) {
                // If fail, go back
                System.err.println("Gemini API failed, using fallback: " + e.getMessage());
            }
        }

        Player activePlayer;
        boolean bettingEnded;
        do {
            bettingEnded = false;
            for (Integer i : turnOrder) {
                activePlayer = players.get(i);
                // Check if Action.Undecided is right.
                setToPlay(getToCall(this.pots,activePlayer));
                System.out.println(activePlayer.getName() + ", to call: " + this.toPlay + ", Action:" + activePlayer.getAction());
                if (!Action.hasFolded(activePlayer.getAction()) && activePlayer.getAction().equals(Action.UNDECIDED)) {
                    if (testAllPlayersFolded(activePlayer)) {
                        endBetting(activePlayer);
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
        postBetting();
    }

    public void  checkCreateNewPot(Player activePlayer) {
        if (activePlayer.getAction().equals(Action.ALLIN) && activePlayer.getActiveBet() < this.toPlay) {
            int newPotSize = activePlayer.getActiveBet();
            System.out.println(activePlayer.getName() + " is creating a new pot by " + activePlayer.getAction() );
            System.out.println("Creating a type 2 sidePot");
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
                    System.out.println(activePlayer.getName() + " is creating a new pot by " + activePlayer.getAction() );
                    System.out.println("Creating a type 1 sidePot");
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
            Random random = new Random();
            int thinkTime = random.nextInt((2000-500)+1) + 500;
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

        if (activePlayer instanceof AiPlayer) {
            ((AiPlayer) activePlayer).setResponse(aiDecisions.get(activePlayer));
        }
        activePlayer.play(this.pots);
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
        this.roundLog.add(new RoundLogEntry(this.roundStatus + " round has ended."));
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

        System.out.println("Number of Undecided players: "+numUndecided);
        System.out.println("Number of players Raised: "+numRaise);
        System.out.println("Number of players All in: "+numAllIn);
        System.out.println("Number of players checked: "+numCheck);
        System.out.println("Number of players called: "+numCall);
        System.out.println("Number of players Folded: "+numFold);
        System.out.println();

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

        System.out.println("Condition 1: "+cond1);
        System.out.println("Condition 2: "+cond2);
        System.out.println("Condition 3: "+cond3);
        System.out.println("Condition 4: "+cond4);
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

    public void checkBetType() {
        BetTypeLogic.executeBets(BetType.NORMAL);
        betting();

        switch (betType) {
            case ENDROUND -> end();
            case SKIP2SHOWDOWN -> setRoundStatus(RoundStatus.SHOWDOWN);
            case NORMAL, SIDEPOT -> betting();
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
                        this.roundLog.add(new RoundLogEntry(p,p.getName() + " is a winner, "
                                + proNoun + " won " + Math.floor((double) pot.getPotSize()/numWinners) + "."));
                    }
                }
            }
        }
        announceShowdownResults();

        try {
            Thread.sleep(5000);
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