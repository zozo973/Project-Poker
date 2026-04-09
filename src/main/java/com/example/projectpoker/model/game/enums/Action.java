package com.example.projectpoker.model.game.enums;

public enum Action {
    UNDECIDED("undecided action"),
    RAISE("raise"),
    CHECK("check"),
    CALL("call"),
    FOLD("fold"),
    ALLIN("all in");

    private final String description;

    Action(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    public static boolean isBet(Action action) { return (action == RAISE || action == CALL); }

    public boolean endBettingActions(Action action) { return (action == FOLD || action == CALL || action == CHECK);}
}