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

    public String getDescription() { return description; }

    public static boolean hasFolded(Action action) {return action.equals(FOLD); }

    public static boolean isRaise(Action action) { return (action.equals(RAISE) ||  action.equals(ALLIN)); }

    public static boolean isBet(Action action) { return (action.equals(RAISE) ||  action.equals(ALLIN) || action.equals(CALL)); }

    public static boolean endBettingActions(Action action) { return (action == FOLD || action == CALL || action == CHECK);}
}