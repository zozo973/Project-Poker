package com.example.projectpoker.model.game.enums;

public enum RoundStatus {

    BLINDS("Set the blinds",1),
    DEAL("Deal the cards",2),
    BETTING1("First round of betting",3),
    FLOP("Flop",4),
    BETTING2("Second round of betting",5),
    TURN("Turn",6),
    BETTING3("Third round of betting",7),
    RIVER("River",8),
    BETTING4("Final round of betting",9),
    SHOWDOWN("Showdown",10),
    END("Game over",11),
    UNINITIALISED("uninitialised",12);


    private final String description;
    private final int step;

    RoundStatus (String description, int step) {
        this.description = description;
        this.step = step;
    }

    private static RoundStatus getRoundStatus(int i) {
        switch (i){
            case 1: return BLINDS;
            case 2: return DEAL;
            case 3: return BETTING1;
            case 4: return FLOP;
            case 5: return BETTING2;
            case 6: return TURN;
            case 7: return BETTING3;
            case 8: return RIVER;
            case 9: return BETTING4;
            case 10: return SHOWDOWN;
            case 11: return END;
            default: return UNINITIALISED;
        }
    }

    public static RoundStatus stepRoundStatus(RoundStatus status) {
        if (status == END) System.err.println("Cannot step the Round status as the round has ended");
        return getRoundStatus(status.ordinal() + 1);
    }
}
