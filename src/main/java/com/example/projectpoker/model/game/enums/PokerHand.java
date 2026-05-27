package com.example.projectpoker.model.game.enums;

public enum PokerHand {
    // ENUM constants
    HIGHCARD("High card"),
    ONEPAIR("Pair"),
    TWOPAIR("Two pair"),
    TRIPLE("Three of a kind"),
    STRAIGHT("Straight"),
    FLUSH("Flush"),
    FULLHOUSE("Full house"),
    QUAD("Four of a kind"),
    STRAIGHTFLUSH("Straight flush"),
    ROYALFLUSH("Royal flush");

    private final String description;

    PokerHand(String description) {
        this.description = description;
    }

    /**
     * Returns the human-readable name of this poker hand category.
     *
     * @return the description string (e.g. "Pair", "Full house", "Royal flush")
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the numeric rank of this hand type used for comparison.
     * Higher values beat lower values (1 = high card, 10 = royal flush).
     *
     * @return the rank value (ordinal + 1), in the range [1, 10]
     */
    public int getValue() {
        return ordinal() + 1;
    }
}
