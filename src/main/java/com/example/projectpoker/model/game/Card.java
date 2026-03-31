package com.example.projectpoker.model.game;


import java.util.ArrayList;
import java.util.Collections;

public class Card {

    private Suit suit;
    private Rank rank;

    // Constructor
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int getValue() {
        return rank.ordinal() + 2;
    }

    // Possibly recode or remove method as it may not be necessary
    // as there should never be the exact same card
    @Override
    public boolean equals(Object o) {
        return (o instanceof Card && ((Card) o).rank == rank && ((Card) o).suit == suit);
    }
}
