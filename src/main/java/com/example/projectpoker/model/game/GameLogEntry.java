package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Roles;

public class GameLogEntry {
    private Player player;
    private int betSize;
    private int toCall;
    private Action action;

    public GameLogEntry(Player player, int toCall, int betSize, Action action) {
        this.player = player;
        this.action = action;
        this.toCall = toCall;
        this.betSize = betSize;
    }

    public GameLogEntry(Player player, int betSize) {
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
        return (String) player.getName() + "decided to" + action.getDescription() + str;
    }
}
