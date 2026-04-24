package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Roles;

public class RoundLogEntry {
    private Player player;
    private int betSize;
    private int toCall;
    private Action action;
    private Pot currentPot;

    public RoundLogEntry(Player player, int toCall, int betSize, Action action, Pot currentPot) {
        this.player = player;
        this.action = action;
        this.toCall = toCall;
        this.betSize = betSize;
        this.currentPot = currentPot;
    }

    public RoundLogEntry(Player player, int betSize) {
        this.player = player;
        this.betSize = betSize;
        if (betSize == 0) {
            this.action = Action.CHECK;
        } else if (betSize == -1) {
            this.action = Action.FOLD;
            if (player.getRole() == Roles.SMALLBLIND) {
            }
        }
    }

    public String displayGameLogEntry(){
        String str;
         if (action == Action.RAISE) str = " by " + Integer.toString(betSize - toCall) + "dollars.";
         else if (action == Action.CALL) str = Integer.toString(toCall) + "dollars to play.";
         else { str = "."; }
        String potDescription = currentPot == null ? "" : " The current Pot is now at " + currentPot.getPotSize() + " dollars.";
        return (String) player.getName() + "decided to" + action.getDescription() + str + potDescription;
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
