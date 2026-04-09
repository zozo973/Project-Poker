package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
public class GameLogEntry {
    private final Player player;
    private final int betSize;
    private final int toCall;
    private final Action action;

    public GameLogEntry(Player player, int toCall, int betSize, Action action) {
        this.player = player;
        this.action = action;
        this.toCall = toCall;
        this.betSize = betSize;
    }

    public GameLogEntry(Player player, int betSize) {
        this.player = player;
        this.betSize = betSize;
        this.toCall = 0;
        if (betSize == 0) {
            this.action = Action.CHECK;
        } else if (betSize == -1) {
            this.action = Action.FOLD;
        } else {
            this.action = Action.CALL;
        }
    }

    public String displayGameLogEntry(){
        String str;
         if (action == Action.RAISE) str = " by " + Integer.toString(betSize - toCall) + "dollars.";
         else if (action == Action.CALL) str = Integer.toString(toCall) + "dollars to play.";
         else { str = "."; }
        return (String) player.getName() + "decided to" + action.getDescription() + str;
    }

    public String getPlayerName() {
        return player.getName();
    }

    public int getBetSize() {
        return betSize;
    }

    public int getToCall() {
        return toCall;
    }

    public Action getAction() {
        return action;
    }
}
