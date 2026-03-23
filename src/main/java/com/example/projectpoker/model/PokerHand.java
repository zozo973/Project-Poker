package com.example.projectpoker.model;

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

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return ordinal() + 1;
    }
}
