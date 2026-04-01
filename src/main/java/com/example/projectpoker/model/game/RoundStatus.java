package com.example.projectpoker.model.game;

public enum RoundStatus {

    BLINDS("Set the blinds",1),
    DEAL("Deal the cards",2),
    BETTING1("First round of betting",3),
    FLOP("Flop",4),
    BETTING2("Second round of betting",5),
    TURN("Turn",6),
    BETTING3("Third round of betting",7),
    RIVER("River",8),
    SHOWDOWN("Showdown",9),
    END("Game over",10);

    private final String description;
    private final int step;

    RoundStatus (String description, int step) {
        this.description = description;
        this.step = step;
    }

    private static RoundStatus getRoundStatus(int i) {
        RoundStatus status = END;
        switch (i){
            case 1:
                status = BLINDS;
            case 2:
                status = DEAL;
            case 3:
                status = BETTING1;
            case 4:
                status = FLOP;
            case 5:
                status = BETTING2;
            case 6:
                status = TURN;
            case 7:
                status = BETTING3;
            case 8:
                status = RIVER;
            case 9:
                status = SHOWDOWN;
            case 10:
                status = END;
        }
        return status;
    }

    public static RoundStatus stepRoundStatus(RoundStatus status) {
        if (status == END) System.err.println("Cannot step the Round status as the round has ended");
        return getRoundStatus(status.ordinal() + 1);
    }
}
