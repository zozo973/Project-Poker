package com.example.projectpoker.model;

import com.example.projectpoker.Player;

import java.util.*;

public class PokerGame {

    private ArrayList<Player> players;
    private ArrayList<Roles> roles;
    private int numRounds;
    private int initialBlind;
    private int whenInceaseBlinds;


    // Constructor called when starting a new game of poker
    // @Params
    //      user: The user player data
    //      numPlayer: number of total players,
    //      initBlind: the starting size of the blinds
    //      whenInceaseBlinds: How many rounds need to be played before the blinds increase
    //      gameLength: maximum number of rounds the poker game goes for.
    //      difficulty: affects the intelligence, risk taking and starting cash of the AI players
    //
    public PokerGame(Player user, int numPlayers, int initBlind, int whenInceaseBlinds, int gameLength, Difficulty difficulty) {
        this.players = new ArrayList<Player>();
        players.add(user);
        this.players = initAiPlayers(players,user.getBalance(),numPlayers,difficulty);
        this.initialBlind = initBlind;
        this.whenInceaseBlinds = whenInceaseBlinds;
        this.numRounds = gameLength;
        this.roles = new ArrayList<Roles>();
    }

    private ArrayList<Roles> delegateRoles(ArrayList<Player> players) {
        ArrayList<Roles> roles = new ArrayList<>();
        roles.add(Roles.DEALER); roles.add(Roles.BIGBLIND); roles.add(Roles.SMALLBLIND);
        if (players.size()>3){
            for (int i = 0; i < players.size()-3; i++){
                roles.add(Roles.PLAYER);
            }
        }
        Collections.reverse(roles);
        return roles;
    }


    private ArrayList<Player> initAiPlayers(ArrayList<Player> players, int userBalance, int numPlayers, Difficulty difficulty) {
        for (int i = numPlayers-1; i > 0 ; i--) {
            players.add(new AiPlayer(difficulty,userBalance));
        }
        return players;
    }
}
