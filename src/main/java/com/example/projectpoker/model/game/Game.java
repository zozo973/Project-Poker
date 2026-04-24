package com.example.projectpoker.model.game;

import com.example.projectpoker.database.DatabaseManager;
import com.example.projectpoker.handler.RoundPlayerListener;
import com.example.projectpoker.handler.RoundStatusChangeHandler;
import com.example.projectpoker.model.User;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;

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

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final RoundPlayerListener roundPlayerListener = new RoundPlayerListener();
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
    private ArrayList<ArrayList<RoundLogEntry>> GameLog;
    private RoundStatusChangeHandler roundHandler;
    private final int startingUserBalance;
    private int handsPlayed;
    private final User userProfile;
    private int gameSessionId;


    // Constructor called when starting a new game of poker
    // @Params
    //      user: The user player data
    //      numPlayer: number of total players,
    //      initBlind: the starting size of the blinds
    //      whenIncreaseBlinds: How many rounds need to be played before the blinds increase
    //      gameLength: maximum number of rounds the poker game goes for.
    //      difficulty: affects the intelligence, risk taking and starting cash of the AI players
    //

    public Game(Player user, int userBalance, int numPlayers, int initBlind, int whenIncreaseBlinds, int gameLength, Difficulty difficulty) {
        this(user, null, userBalance, numPlayers, initBlind, whenIncreaseBlinds, gameLength, difficulty);
    }

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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public ArrayList<ArrayList<RoundLogEntry>> getGameLog() { return GameLog; }

    public void setGameLog(ArrayList<ArrayList<RoundLogEntry>> gameLog) { GameLog = gameLog; }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus (GameStatus gameStatus) {
        var oldVal = this.gameStatus;
        this.gameStatus = gameStatus;
        pcs.firePropertyChange("state",oldVal,this.gameStatus);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        if (!(players.getFirst().getPropertyChangeListener("roundPlayerListener") instanceof RoundPlayerListener[])) {
            for (Player p : players) {
                p.addPropertyChangeListener("roundPlayerListener", this.roundPlayerListener);
            }
        }
        var oldVal = this.players;
        this.players = players;
        pcs.firePropertyChange("players",oldVal,this.players);
    }

    public void createNextRound() {
        // Round round = new Round(players, blindSize, gameSessionId, handsPlayed + 1);
        Round round = new Round(players, blindSize);
        pcs.firePropertyChange("round",this.round,round);
        round.addPropertyChangeListener(roundHandler);
        setRound(round);
        roundPlayerListener.setRound(round);
    }

    public int getNumRoundsLeft() { return numRoundsLeft; }

    public void setNumRoundsLeft(int numRoundsLeft) { this.numRoundsLeft = numRoundsLeft; }

    public Round getRound() { return this.round; }

    private void setRound(Round round) { this.round = round; }

    public int getBlindSize() { return blindSize; }

    public void setBlindSize(int blindSize) {
        var oldVal = this.blindSize;
        this.blindSize = blindSize;
        pcs.firePropertyChange("blindSize",oldVal,this.blindSize);
    }

    public ArrayList<AiPlayer> getAiPlayers() {
        ArrayList<AiPlayer> AiPlayers = new ArrayList<>();
        for (Player p : players) {
            if (p instanceof AiPlayer) AiPlayers.add((AiPlayer) p);
        }
        return AiPlayers;
    }

    public void setRoundHandler(RoundStatusChangeHandler roundHandler) { this.roundHandler = roundHandler; }

    public RoundStatusChangeHandler getRoundHandler() { return this.roundHandler; }

    public int findUserIndex() {
        int i = 0;
        for (Player p : players) {
            if (!(p instanceof AiPlayer)) return i;
            i++;
        }
        return i;
    }

    public Player getUser() {
        for (Player p : players) {
            if (!(p instanceof AiPlayer)) return p;
        }
        throw new IllegalStateException("There is no User in players, only Ai player");
    }

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

    public void start() {
        // Valid game before starting
        setGameStatus(GameStatus.RUNNING);

        startNextRound();
    }

    public void startNextRound() {

        if (gameStatus != GameStatus.RUNNING)
            return;

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

        System.out.println(round.getRoundStatus());

        round.init();

        System.out.println(round.getRoundStatus());

        round.start();
    }
    public void onRoundEnded() {

        GameLog.add(round.getRoundLog());

        nextRoundInitialisation();

        startNextRound();
    }


    public void start(boolean test) {
        if (!test) return;
        // Valid game before starting
        setGameStatus(GameStatus.RUNNING);
        while (gameStatus == GameStatus.RUNNING) {
            // createNextRound();
            // System.out.println(round.getRoundStatus());
            // this.round.init();
            // System.out.println(round.getRoundStatus());
            //  this.round.start();
            // System.out.println(round.getRoundStatus());

            // Loss Condition
            if (getUser().getBalance() == 0) { end(); break; }
            else if (this.numRoundsLeft == 0) { end(); break; }
            else if (this.players.size() == 1 && !(this.players.getFirst() instanceof AiPlayer)) { end(); break; }
            this.GameLog.add(round.getRoundLog());
            nextRoundInitialisation();
        }
    }

    public void end() {
        setGameStatus(GameStatus.ENDED);
        DatabaseManager.finalizeGameSession(gameSessionId, userProfile, this, getUser());
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
        for (int i = numPlayers - 1; i > 0; i--) {
            players.add(new AiPlayer(difficulty, getUser().getBalance()));
        }
        Collections.reverse(players);
        return players;
    }

    public void tryIncreaseBlind() {
        if (gameLength != numRoundsLeft && (gameLength - numRoundsLeft) % whenIncreaseBlinds == 0) {
            setBlindSize(this.blindSize*2);
        }
    }

    // method to check if any players have lost all there money or left the game.
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

    public int getHandsPlayed() {
        return handsPlayed;
    }

    public int getStartingUserBalance() {
        return startingUserBalance;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public int getUserBuyIn() {
        return userBuyIn;
    }

    public int getWhenIncreaseBlinds() {
        return whenIncreaseBlinds;
    }

    public int getGameLength() {
        return gameLength;
    }
}