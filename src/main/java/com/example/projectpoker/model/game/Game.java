package com.example.projectpoker.model.game;

import com.example.projectpoker.database.DatabaseManager;
import com.example.projectpoker.model.User;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;
import com.example.projectpoker.model.game.enums.RoundStatus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;


public class Game {

    // Game Events
    //      gameStatus Change
    //      blindSize Change
    //      players Change
    //      Round Change

    private GameStatus gameStatus;
    private ArrayList<Player> players;
    private int numRoundsLeft;
    private final int gameLength;
    private int blindSize;
    private final int whenIncreaseBlinds;
    private final Difficulty difficulty;
    private int numPlayers;
    private final int userBuyIn;
    private Round round;
    private ArrayList<RoundLog> GameLog;
    private boolean roundAdvanceInProgress;
    private final int startingUserBalance;
    private int handsPlayed;
    private final User userProfile;
    private int gameSessionId;
    private boolean sessionFinalized;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final PropertyChangeListener playerActionListener = evt -> {
        if (!"action".equals(evt.getPropertyName())) {
            return;
        }

        if (evt.getNewValue() == Action.FORFEIT && round != null) {
            round.removeForfeited();
            numPlayers = players.size();
            pcs.firePropertyChange("players", null, this.players);
        }
    };

    /** Constructor called when starting a new game of poker
     * @param user: The users player data
     * @param userProfile: users profile in database.
     * @param numPlayers: number of total players,
     * @param initBlind: the starting size of the blinds
     * @param whenIncreaseBlinds: How many rounds need to be played before the blinds increase
     * @param gameLength: maximum number of rounds the poker game goes for.
     * @param difficulty: affects the intelligence, risk taking and starting cash of the AI players
      */

    public Game(Player user, User userProfile, int userBalance, int numPlayers, int initBlind, int whenIncreaseBlinds, int gameLength, Difficulty difficulty) {
        this.players = new ArrayList<>();
        user.setBalance(userBalance);
        players.add(user);
        this.numPlayers = numPlayers;
        this.userBuyIn = userBalance;
        this.difficulty = difficulty;
        this.blindSize = initBlind;
        this.whenIncreaseBlinds = whenIncreaseBlinds;
        this.gameLength = gameLength;
        this.numRoundsLeft = gameLength;
        this.GameLog = new ArrayList<>();
        this.startingUserBalance = user.getBalance();
        this.handsPlayed = 0;
        this.userProfile = userProfile;
        this.gameSessionId = -1;
    }

    /** Secondary Constructor used for testing the game class separate from the database
     */

    public Game(Player user, int userBalance, int numPlayers, int initBlind, int whenIncreaseBlinds, int gameLength, Difficulty difficulty) {
        this(user, null, userBalance, numPlayers, initBlind, whenIncreaseBlinds, gameLength, difficulty);
    }

    /**
     * Registers a listener for all property change events fired by this game
     * (e.g. gameStatus, blindSize, players, round).
     *
     * @param listener the {@link PropertyChangeListener} to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a previously registered global property change listener.
     *
     * @param listener the {@link PropertyChangeListener} to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Returns the log of all completed rounds, each captured as a {@link RoundLog}.
     *
     * @return the list of round logs accumulated during this game
     */
    public ArrayList<RoundLog> getGameLog() { return GameLog; }

    /**
     * Returns the current status of the game (e.g. INITIALISED, RUNNING, ENDED).
     *
     * @return the current {@link GameStatus}
     */
    public GameStatus getGameStatus() {
        return gameStatus;
    }

    /**
     * Sets the game status and fires a property change event to notify observers.
     *
     * @param gameStatus the new {@link GameStatus} to assign
     */
    public void setGameStatus (GameStatus gameStatus) {
        var oldVal = this.gameStatus;
        this.gameStatus = gameStatus;
        pcs.firePropertyChange("state",oldVal,this.gameStatus);
    }

    /**
     * Returns all players currently in the game, including AI players.
     *
     * @return the mutable list of {@link Player} objects
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     * Replaces the player list, syncs property-change listeners, and fires a "players" event.
     *
     * @param players the new list of {@link Player} objects to use
     */
    public void setPlayers(ArrayList<Player> players) {
        var oldVal = this.players;
        syncPlayerActionListeners(oldVal, players);
        this.players = players;
        pcs.firePropertyChange("players",oldVal,this.players);
    }

    /**
     * Prepares and fires a new {@link Round} for the next hand,
     * applying the current blind size to all players.
     */
    public void createNextRound() {
        for (Player p : this.players) p.setMinBet((int) Math.round (this.blindSize*0.5));
        Round round = new Round(players, blindSize, gameSessionId, handsPlayed + 1);
        pcs.firePropertyChange("round",this.round,round);
        setRound(round);
    }

    /**
     * Returns the number of rounds remaining before the game ends.
     *
     * @return rounds left as a non-negative integer
     */
    public int getNumRoundsLeft() { return numRoundsLeft; }

    /**
     * Sets the number of rounds remaining.
     *
     * @param numRoundsLeft the new round count
     */
    public void setNumRoundsLeft(int numRoundsLeft) { this.numRoundsLeft = numRoundsLeft; }

    /**
     * Returns the most recently created round, or {@code null} if no round has started yet.
     *
     * @return the current {@link Round}
     */
    public Round getRound() { return this.round; }

    private void setRound(Round round) { this.round = round; }

    /**
     * Returns the current blind size used to determine minimum bets.
     *
     * @return the blind size in chips
     */
    public int getBlindSize() { return blindSize; }

    /**
     * Updates the blind size and fires a "blindSize" property change event.
     *
     * @param blindSize the new blind size in chips
     */
    public void setBlindSize(int blindSize) {
        var oldVal = this.blindSize;
        this.blindSize = blindSize;
        pcs.firePropertyChange("blindSize",oldVal,this.blindSize);
    }

    /**
     * Returns a list containing only the AI-controlled players in the game.
     *
     * @return list of {@link AiPlayer} instances (may be empty if there are none)
     */
    public ArrayList<AiPlayer> getAiPlayers() {
        ArrayList<AiPlayer> AiPlayers = new ArrayList<>();
        for (Player p : players) {
            if (p instanceof AiPlayer) AiPlayers.add((AiPlayer) p);
        }
        return AiPlayers;
    }

    /**
     * Returns the index of the human (non-AI) player in the players list.
     *
     * @return zero-based index of the user player, or the list size if no user is found
     */
    public int findUserIndex() {
        int i = 0;
        for (Player p : players) {
            if (!(p instanceof AiPlayer)) return i;
            i++;
        }
        return i;
    }

    /**
     * Returns the human (non-AI) player in the game.
     *
     * @return the user's {@link Player} object
     * @throws IllegalStateException if no non-AI player exists in the players list
     */
    public Player getUser() {
        for (Player p : players) {
            if (!(p instanceof AiPlayer)) return p;
        }
        throw new IllegalStateException("There is no User in players, only Ai player");
    }

    /**
     * Initialises the game by creating AI players, assigning roles, and opening a database session.
     * Also checks whether the blind should be increased before the first round.
     */
    public void init() {
        tryIncreaseBlind();
        setPlayers(
          RoleUtil.delegateRoles(
            initAiPlayers(
                    players,
                    numPlayers,
                    difficulty
            ), new int[]{0, 1, 2}
          )
        );
        this.gameSessionId = DatabaseManager.createGameSession(userProfile, this, getUser());
        setGameStatus(GameStatus.INITIALISED);
    }

    /**
     * Sets the game status to RUNNING and starts the first round.
     */
    public void start() {
        // Valid game before starting
        setGameStatus(GameStatus.RUNNING);

        startNextRound();
    }

    /**
     * Starts the next round if the game is running and the end conditions have not been met.
     * Guards against concurrent round starts using a synchronized flag.
     * End conditions include: user balance = 0, no rounds left, or only user remains.
     */
    public void startNextRound() {

        synchronized (this) {
            // Guarding behaviour so that multiple rounds cant be started simultaneously
            if (roundAdvanceInProgress) {
                return;
            }
            roundAdvanceInProgress = true;
        }

        try {
            if (gameStatus != GameStatus.RUNNING) {return;}

            clearPlayerHands();

            // Loss conditions
            if (getUser().getBalance() == 0) {
                end();
                return;
            }

            if (numRoundsLeft == 0) {
                end();
                return;
            }

            if (players.size() == 1 &&
                    !(players.getFirst() instanceof AiPlayer)) {
                end();
                return;
            }
            createNextRound();
            round.init();
            round.start();
        } finally {
            synchronized (this) {
                roundAdvanceInProgress = false;
            }
        }
    }

    /**
     * Called when the current round has finished.
     * Logs the round, rotates roles, decrements the round counter, and begins the next round.
     */
    public void onRoundEnded() {

        if (round == null || round.getRoundStatus() != RoundStatus.END) {
            return;
        }


        GameLog.add(round.getFinalLog());
        countCompletedRound();

        nextRoundInitialisation();

        startNextRound();
    }

    private void clearPlayerHands() {
        if (players == null) {
            return;
        }

        for (Player player : players) {
            player.getPlayerHand().clear();
        }
    }

    /**
     * Ends the game by persisting the session to the database.
     */
    public void end() {
        finishSession();
    }

    /**
     * Closes the game session safely from any thread (e.g. when the window is closed mid-game).
     * Idempotent — calling this more than once has no additional effect.
     */
    public synchronized void closeSession() {
        finishSession();
    }

    private synchronized void finishSession() {
        if (sessionFinalized) {
            return;
        }

        // Guard against double-saving if the game ends normally and the window also closes.
        sessionFinalized = true;
        if (round != null) {
            round.requestStop();
        }
        countCompletedRound();
        setGameStatus(GameStatus.ENDED);
        DatabaseManager.finalizeGameSession(gameSessionId, userProfile, this, getUser());
    }

    private void countCompletedRound() {
        if (round == null
                || round.isPersisted()
                || round.getRoundStatus() != RoundStatus.END) {
            return;
        }

        handsPlayed++;
        DatabaseManager.recordRound(gameSessionId, round);
        round.markPersisted();
    }

    private void nextRoundInitialisation() {
        setPlayers(
            RoleUtil.delegateRoles(
                this.players,
                RoleUtil.stepRoleIndices(
                    this.players
                )
            )
        );
        this.numRoundsLeft--;
    }

    private ArrayList<Player> initAiPlayers(ArrayList<Player> players, int numPlayers, Difficulty difficulty) {
        for (int i = 0; i < numPlayers - 1; i++) {
            players.add(new AiPlayer(difficulty, getUser().getBalance()));
            players.get(i+1).setName("AI player " + (i+1));
        }
        Collections.reverse(players);
        return players;
    }

    private void syncPlayerActionListeners(ArrayList<Player> oldPlayers, ArrayList<Player> newPlayers) {
        if (oldPlayers != null) {
            for (Player player : oldPlayers) {
                player.removePropertyChangeListener("action", playerActionListener);
            }
        }

        if (newPlayers == null) {
            return;
        }

        for (Player player : newPlayers) {
            player.removePropertyChangeListener("action", playerActionListener);
            player.addPropertyChangeListener("action", playerActionListener);
        }
    }


    /**
     * Checks for players whose action is FORFEIT and removes them from the active player list.
     * Updates the total player count accordingly.
     */
    public void checkForfeitedPlayers() {
        ArrayList<Player> activePlayers = new ArrayList<>();
        for (Player p : this.players) {
            if (!p.getAction().equals(Action.FORFEIT)) activePlayers.add(p);
        }
        if (!activePlayers.equals(this.players)) {
            setPlayers(activePlayers);
            this.numPlayers = activePlayers.size();
        }
    }

    /**
     * Returns the total number of hands (rounds) completed so far in this game.
     *
     * @return hands played count
     */
    public int getHandsPlayed() {
        return handsPlayed;
    }

    /**
     * Returns the chip balance the user started the game with (their buy-in this session).
     *
     * @return the starting user balance in chips
     */
    public int getStartingUserBalance() {
        return startingUserBalance;
    }

    /**
     * Returns the AI difficulty level for this game.
     *
     * @return the {@link Difficulty} enum value
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Returns the total number of players (human + AI) in the game.
     *
     * @return the player count
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * Returns the chip amount the user bought in with at the start of this game.
     *
     * @return the user buy-in amount
     */
    public int getUserBuyIn() {
        return userBuyIn;
    }

    /**
     * Returns the number of rounds that must pass before the blind size is doubled.
     *
     * @return the blind-increase interval in rounds
     */
    public int getWhenIncreaseBlinds() {
        return whenIncreaseBlinds;
    }

    /**
     * Returns the total number of rounds this game is configured to run.
     *
     * @return the game length in rounds
     */
    public int getGameLength() {
        return gameLength;
    }

    /**
     * Doubles the blind size if the required number of rounds since the last increase have elapsed.
     * Has no effect on the very first round or if the increase interval has not been reached.
     */
    public void tryIncreaseBlind() {
        if (gameLength != numRoundsLeft && (gameLength - numRoundsLeft) % whenIncreaseBlinds == 0) {
            setBlindSize(this.blindSize*2);
        }
    }
}
