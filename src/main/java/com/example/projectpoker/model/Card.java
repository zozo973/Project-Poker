package com.example.projectpoker.model;


    public class Card {

    private CardDeck.Suit suit;
    private CardDeck.Rank rank;

    // Constructor
    public Card(CardDeck.Suit suit, CardDeck.Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public CardDeck.Suit getSuit() {
        return suit;
    }

    public CardDeck.Rank getRank() {
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
