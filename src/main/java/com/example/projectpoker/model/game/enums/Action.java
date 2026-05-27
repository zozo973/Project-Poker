package com.example.projectpoker.model.game.enums;

public enum Action {
    UNDECIDED("undecided action"),
    RAISE("raise"),
    CHECK("check"),
    CALL("call"),
    FORFEIT("forfeit"),
    FOLD("fold"),
    ALLIN("all in");

    private final String description;

    Action(String description) {
        this.description = description;
    }

    /**
     * Returns the human-readable description of this action.
     *
     * @return the description string (e.g. "fold", "raise", "check")
     */
    public String getDescription() { return description; }

    /**
     * Returns whether the given action represents a fold (the player has left the hand).
     *
     * @param action the {@link Action} to test
     * @return {@code true} if {@code action} is {@link #FOLD}
     */
    public static boolean hasFolded(Action action) { return action.equals(FOLD); }

    /**
     * Returns whether the given action means the player is still active in the hand.
     *
     * @param action the {@link Action} to test
     * @return {@code true} if the action is neither {@link #FOLD} nor {@link #FORFEIT}
     */
    public static boolean isInGame(Action action) { return !(action.equals(FOLD) || action.equals(FORFEIT)); }

    /**
     * Returns whether the given action qualifies as a raise (increases the bet above the current level).
     *
     * @param action the {@link Action} to test
     * @return {@code true} if the action is {@link #RAISE} or {@link #ALLIN}
     */
    public static boolean isRaise(Action action) { return (action.equals(RAISE) ||  action.equals(ALLIN)); }

    /**
     * Returns whether the given action involves placing chips into the pot.
     *
     * @param action the {@link Action} to test
     * @return {@code true} if the action is {@link #RAISE}, {@link #ALLIN}, or {@link #CALL}
     */
    public static boolean isBet(Action action) { return (action.equals(RAISE) ||  action.equals(ALLIN) || action.equals(CALL)); }

    /**
     * Returns whether the given action concludes the player's turn without raising
     * (i.e. the betting round can potentially end after this action).
     *
     * @param action the {@link Action} to test
     * @return {@code true} if the action is {@link #FOLD}, {@link #CALL}, or {@link #CHECK}
     */
    public static boolean endBettingActions(Action action) { return (action == FOLD || action == CALL || action == CHECK);}
}