package com.example.projectpoker.model;

import java.util.*;

public class PokerGame {

    private ArrayList<Player> players;
    private int numRoundsLeft;
    private int gameLength;
    private int blindSize;
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
        this.players = new ArrayList<>();
        players.add(user);
        this.players = delegateRoles(initAiPlayers(players,user.getBalance(),numPlayers,difficulty), new int[] {0,1,2});
        this.blindSize = initBlind;
        this.whenInceaseBlinds = whenInceaseBlinds;
        this.gameLength = gameLength;
        this.numRoundsLeft = gameLength;
        // Method for loading visual game features
    }

    private ArrayList<Player> initAiPlayers(ArrayList<Player> players, int userBalance, int numPlayers, Difficulty difficulty) {
        for (int i = numPlayers-1; i > 0 ; i--) {
            players.add(new AiPlayer(difficulty,userBalance));
        }
        Collections.reverse(players);
        return players;
    }

    private ArrayList<Player> delegateRoles(ArrayList<Player> players, int[] roleIndices) {
        for (Player p : players) {
            p.setRole(Roles.PLAYER);
        }
        players.get(roleIndices[0]).setRole(Roles.DEALER);
        players.get(roleIndices[1]).setRole(Roles.SMALLBLIND);
        players.get(roleIndices[2]).setRole(Roles.BIGBLIND);
        return players;
    }

    private int[] findRoleIndices() {
        int[] roleIndices = {0, 0, 0};
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getRole() == Roles.DEALER) {
                roleIndices[0] = i;
            } else if (players.get(i).getRole() == Roles.SMALLBLIND) {
                roleIndices[1] = i;
            } else if (players.get(i).getRole() == Roles.BIGBLIND) {
                roleIndices[2] = i;
            }
        }
        return roleIndices;
    }

    private int[] stepRoleIndices() {
        int[] roleIndices = findRoleIndices();
        if (roleIndices[0] == players.size() - 3) {
            roleIndices[0] += 1;
            roleIndices[1] += 1;
            roleIndices[2] = 0;
        } else if (roleIndices[0] == players.size() - 2) {
            roleIndices[0] += 1;
            roleIndices[1] = 0;
            roleIndices[2] = 1;
        } else if (roleIndices[0] == players.size() - 1) { roleIndices = new int[] {0,1,2}; }
        else {
            roleIndices[0] += 1;
            roleIndices[1] += 1;
            roleIndices[2] += 1;
        }
        return roleIndices;
    }


    private void tryIncreaseBlind() {
        if ((gameLength - numRoundsLeft) == whenInceaseBlinds) {
            this.blindSize = blindSize * 2;
        }
    }

    public void newRound(ArrayList<Player> players) {
        tryIncreaseBlind();
        PokerRound round = new PokerRound(delegateRoles(players,stepRoleIndices()),blindSize,findRoleIndices());
        // methods to play round

        // once round ends
        ArrayList<GameLogEntry> roundLog = round.getRoundLog();
        // method for sending log to database




    }
}
