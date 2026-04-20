package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Roles;

public class RoundLogEntry {
    private final Player player;
    private final int betSize;
    private int toCall;
    private Action action;
    private Pot currentPot;
    private String entryDescription;

    // RoundLogEntry constructor for inputting specific entryDescriptions into the RoundLogEntry
    public RoundLogEntry(Player player, String entryDescription) {

        this.player = player;
        this.betSize = 0;
        this.entryDescription = entryDescription;
    }

    // Constructor for a new pot being created
    public RoundLogEntry(Player player, Pot currentPot) {
        this.player = player;
        this.betSize = 0;
        this.currentPot = currentPot;
        this.entryDescription = player.getName() + " has created a new side pot, with a value of " + currentPot.getPotSize();
    }

    // Default constructor for calling, raising or all-in actions
    public RoundLogEntry(Player player, int toCall, int betSize, Action action, Pot currentPot) {
        this.player = player;
        this.action = action;
        this.toCall = toCall;
        this.betSize = betSize;
        this.currentPot = currentPot;
        setEntryDescription();
    }

    // Constructor for checking or folding actions
    public RoundLogEntry(Player player, int betSize) {
        this.player = player;
        this.action = player.getAction();
        this.betSize = betSize;
        this.entryDescription = player.getName() + " decided to " + action.getDescription();
    }

    public Pot getCurrentPot() { return currentPot; }

    public void setCurrentPot(Pot currentPot) { this.currentPot = currentPot; }

    private void setEntryDescription() {
        String str;
         if (action == Action.RAISE) str = " by " + Integer.toString(betSize - toCall) + "dollars.";
         else if (action == Action.CALL) str = Integer.toString(toCall) + "dollars to play.";
         else { str = "."; }
        this.entryDescription = (String) player.getName() + " decided to " + action.getDescription() + str + " The current Pot is now at " + currentPot.getPotSize() + " dollars.";
    }

    public String getEntryDescription() { return this.entryDescription; }
}
