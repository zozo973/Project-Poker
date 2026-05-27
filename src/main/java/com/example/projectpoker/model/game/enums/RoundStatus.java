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

    /**
     * Returns the human-readable description of this round status.
     *
     * @return the description string (e.g. "Set the blinds", "Deal the cards")
     */
    public String getDescription() { return description; }

    private static RoundStatus getRoundStatus(int i) {
        switch (i){
            case 0: return BLINDS;
            case 1: return DEAL;
            case 2: return BETTING1;
            case 3: return FLOP;
            case 4: return BETTING2;
            case 5: return TURN;
            case 6: return BETTING3;
            case 7: return RIVER;
            case 8: return BETTING4;
            case 9: return SHOWDOWN;
            case 10: return END;
            default: return UNINITIALISED;
        }
    }

    /**
     * Advances the given round status by one step in the standard poker sequence.
     * Returns {@link #END} unchanged if it has already reached that state.
     * Sequence: BLINDS → DEAL → BETTING1 → FLOP → BETTING2 → TURN → BETTING3 → RIVER → BETTING4 → SHOWDOWN → END
     *
     * @param status the current {@link RoundStatus} to advance from
     * @return the next {@link RoundStatus} in the sequence, or {@link #END} if already at the end
     */
    public static RoundStatus stepRoundStatus(RoundStatus status) {
        if (status == END) {
            return END;
        }
            return getRoundStatus(status.ordinal() + 1);
    }
}
