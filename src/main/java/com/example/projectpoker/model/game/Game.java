package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;
import com.example.projectpoker.model.game.oberserver.AbsSubject;

import java.util.ArrayList;
import java.util.Collections;


public class Game extends AbsSubject {

    private GameStatus state;
    private ArrayList<Player> players;
    private int numRoundsLeft;
    private int gameLength;
    private int blindSize;
    private int whenInceaseBlinds;
    private Difficulty difficulty;
    private int numPlayers;
    private int userBalance;


    // Constructor called when starting a new game of poker
    // @Params
    //      user: The user player data
    //      numPlayer: number of total players,
    //      initBlind: the starting size of the blinds
    //      whenInceaseBlinds: How many rounds need to be played before the blinds increase
    //      gameLength: maximum number of rounds the poker game goes for.
    //      difficulty: affects the intelligence, risk taking and starting cash of the AI players
    //

    public Game(Player user, int userBalance, int numPlayers, int initBlind, int whenInceaseBlinds, int gameLength, Difficulty difficulty) {
        this.players = new ArrayList<>();
        players.add(user);
        this.numPlayers = numPlayers;
        this.userBalance = userBalance;
        this.difficulty = difficulty;
        this.blindSize = initBlind;
        this.whenInceaseBlinds = whenInceaseBlinds;
        this.gameLength = gameLength;
        this.numRoundsLeft = gameLength;
        //GameContext gameContext = new GameContext();
        // Method for loading visual game features
    }

    public void init(int numPlayers) {
        this.players = RoleUtil.delegateRoles(initAiPlayers(players, userBalance, numPlayers, difficulty), new int[]{0, 1, 2});
        this.state = GameStatus.INITIALISED;
        notifyObservers(state);
    }

    public void start() {
        // Valid game before starting
        this.state = GameStatus.RUNNING;
        notifyObservers(state);
        while (state == GameStatus.RUNNING) {
            Round round = new Round(players,blindSize);
            round.Init();
            round.Start();
            // Loss Condition
            if (players.getFirst().getBalance() == 0) { end(); break; }
            else if (numRoundsLeft == 0) { end(); break; }
            else if (players.size() == 1 && !(players.getFirst() instanceof AiPlayer)) {end(); break; }
            tryIncreaseBlind();
            this.numRoundsLeft--;
        }
    }

    public void end() {
        this.state = GameStatus.ENDED;
        notifyObservers(state); // notify UI
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
        if ((gameLength - numRoundsLeft) % whenInceaseBlinds == 0) {
            this.blindSize = blindSize * 2;
        }
    }
}