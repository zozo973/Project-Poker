package com.example.projectpoker.model.game;

import com.example.projectpoker.handler.RoundStatusChangeHandler;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;


public class Game {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private GameStatus gameStatus;
    private ArrayList<Player> players;
    private int numRoundsLeft;
    private final int gameLength;
    private int blindSize;
    private final int whenIncreaseBlinds;
    private final Difficulty difficulty;
    private int numPlayers;
    private int userBalance;
    private Round round;
    private RoundStatusChangeHandler roundHandler;


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
        this.players = new ArrayList<>();
        players.add(user);
        this.numPlayers = numPlayers;
        this.userBalance = userBalance;
        this.difficulty = difficulty;
        this.blindSize = initBlind;
        this.whenIncreaseBlinds = whenIncreaseBlinds;
        this.gameLength = gameLength;
        this.numRoundsLeft = gameLength;
        //GameContext gameContext = new GameContext();
        // Method for loading visual game features
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

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
        var oldVal = this.players;
        this.players = players;
        pcs.firePropertyChange("players",oldVal,this.players);
    }

    public void createNextRound() {
        Round round = new Round(players,blindSize);
        pcs.firePropertyChange("round",this.round,round);
        round.addPropertyChangeListener(roundHandler);
        setRound(round);
    }

    public Round getRound() { return this.round; }

    private void setRound(Round round) { this.round = round; }

    public Player getUser() { return players.getFirst(); }

    public ArrayList<AiPlayer> getAiPlayers() {
        ArrayList<AiPlayer> AiPlayers = new ArrayList<>();
        for (Player p : players) {
            if (p instanceof AiPlayer) AiPlayers.add((AiPlayer) p);
        }
        return AiPlayers;
    }

    public void setRoundHandler(RoundStatusChangeHandler roundHandler) { this.roundHandler = roundHandler; }

    public RoundStatusChangeHandler getRoundHandler() { return this.roundHandler; }

    public void init() {
        setPlayers(
          RoleUtil.delegateRoles(
            initAiPlayers(
                    players,
                    userBalance,
                    numPlayers,
                    difficulty
            ), new int[]{0, 1, 2}
          )
        );
        createNextRound();
        setGameStatus(GameStatus.INITIALISED);
    }

    public void start() {
        // Valid game before starting
        setGameStatus(GameStatus.RUNNING);
        while (gameStatus == GameStatus.RUNNING) {
            this.round.init();
            this.round.start();
            // Loss Condition
            if (players.getFirst().getBalance() == 0) { end(); break; }
            else if (numRoundsLeft == 0) { end(); break; }
            else if (players.size() == 1 && !(players.getFirst() instanceof AiPlayer)) {end(); break; }
            tryIncreaseBlind();
            this.numRoundsLeft--;
        }
    }

    public void end() {
        setGameStatus(GameStatus.ENDED); // notify UI
        // save to database
        //      update player balance & records
    }

    private ArrayList<Player> initAiPlayers(ArrayList<Player> players, int userBalance, int numPlayers, Difficulty difficulty) {
        for (int i = numPlayers - 1; i > 0; i--) {
            players.add(new AiPlayer(difficulty, userBalance));
        }
        Collections.reverse(players);
        return players;
    }

    private void tryIncreaseBlind() {
        if ((gameLength - numRoundsLeft) % whenIncreaseBlinds == 0) {
            this.blindSize = blindSize * 2;
        }
    }
}