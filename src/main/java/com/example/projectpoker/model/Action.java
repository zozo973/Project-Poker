package com.example.projectpoker.model;

public enum Action {
    RAISE("raise"),
    CHECK("check"),
    CALL("call"),
    FOLD("fold");

    private final String description;

    Action(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    public boolean isBet(Action action) { return (action == RAISE || action == CALL); }

    public boolean endBettingActions(Action action) { return (action == FOLD || action == CALL || action == CHECK);}
}