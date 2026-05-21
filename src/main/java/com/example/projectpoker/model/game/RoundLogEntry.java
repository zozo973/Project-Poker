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


    /** End of Round Constructor
     *      Creates RoundLogEntry for inputting end of round entry into the RoundLogEntry
     * @param entryDescription: string description.
     */

    public RoundLogEntry(String entryDescription) {
        this.player = null;
        this.action = null;
        this.betSize = 0;
        this.entryDescription = entryDescription;
    }

    /** Player specific Constructor
     *      Creates RoundLogEntry for inputting specific entryDescriptions into the RoundLogEntry
     * @param player: Player object related to the entry Description.
     * @param entryDescription: String of the description.
     */

    public RoundLogEntry(Player player, String entryDescription) {

        this.player = player;
        this.action = player.getAction();
        this.betSize = 0;
        this.entryDescription = entryDescription;
    }

    /** New Pot Constructor
     *      Creates RoundLogEntry for a new pot being created
     * @param player: Player object that created the pot.
     * @param currentPot: Open pot bet into prior to sidePot creation
     */

    public RoundLogEntry(Player player, Pot currentPot) {
        this.player = player;
        this.betSize = 0;
        this.currentPot = currentPot;
        this.entryDescription = player.getName() + " has created a new side pot, with a priority of "+ currentPot.getPotPriority() +" and a value of " + currentPot.getPotSize();
    }

    /** Raising Bet Constructor
     *      Creates RoundLogEntry for calling, raising or all-in actions
     * @param player: Active player.
     * @param toCall: Amount to call after players bet.
     * @param betSize: The integer size of the players bet.
     * @param action: The players action.
     * @param currentPot: The size of the open pot after player bet.
     */

    public RoundLogEntry(Player player, int toCall, int betSize, Action action, Pot currentPot) {
        this.player = player;
        this.action = action;
        this.toCall = toCall;
        this.betSize = betSize;
        this.currentPot = currentPot;
        setEntryDescription();
    }


    /** Non Betting Constructor
     *      Creates RoundLogEntry for checking or folding actions
     * @param player: Active player.
     */

    public RoundLogEntry(Player player) {
        this.player = player;
        this.action = player.getAction();
        this.betSize = 0;
        this.entryDescription = player.getName() + " decided to " + action.getDescription();
    }

    public Pot getCurrentPot() { return currentPot; }

    public void setCurrentPot(Pot currentPot) { this.currentPot = currentPot; }

    private void setEntryDescription() {
        String str;
         if (action == Action.RAISE) str = " by " + Integer.toString(betSize - toCall) + " dollars.";
         else if (action == Action.CALL) str = " " + Integer.toString(toCall) + " dollars to play.";
         else { str = "."; }
        this.entryDescription = (String) player.getName() + " decided to " + action.getDescription() + str + " The current Pot is now at " + currentPot.getPotSize() + " dollars.";
    }

    public String getEntryDescription() { return this.entryDescription; }

    public String displayGameLogEntry(){
        String str;
        if (action == Action.RAISE) str = " by " + Integer.toString(betSize - toCall) + " dollars.";
        else if (action == Action.CALL) str = " " + Integer.toString(toCall) + " dollars to play.";
        else { str = "."; }
        String potDescription = currentPot == null ? "" : " The current Pot is now at " + currentPot.getPotSize() + " dollars.";
        return (String) player.getName() + " decided to " + action.getDescription() + str + potDescription;
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
