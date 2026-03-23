package com.example.projectpoker.GameLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class CardDeck {

    private ArrayList<Card> cardDeck;
    private ArrayList<Card> drawnCards;
    private Random order;

    public enum Suit {
        SPADES,
        CLUBS,
        HEARTS,
        DIAMONDS;
    }

    public enum Rank {
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING,
        ACE;
    }

    public CardDeck() {
        order = new Random();
        drawnCards = new ArrayList<>();
        cardDeck = new ArrayList<Card>(Suit.values().length * Rank.values().length);
        reset();
    }

    public void shuffle() {
        Collections.shuffle(this.cardDeck);
    }

    public void reset() {
        cardDeck.clear();
        drawnCards.clear();
        for (Suit s : Suit.values()) {
            for (Rank r : Rank.values()) {
                Card c = new Card(s,r);
                cardDeck.add(c);
            }
        }
        shuffle();
    }

    public static class Card {

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

    /**
     * Get a random card from the deck, removing it from the deck
     * @ return
     */
    public Card draw() {
        if (cardDeck.isEmpty()) { return null; }
        // int drawnIndex = randInt(0,cardDeck.size()-1);
        Card c = cardDeck.getFirst();
        cardDeck.removeFirst();
        drawnCards.add(c);
        return c;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public int randInt (int min, int max) {
        int randomNum = order.nextInt((max - min) + 1) + min;
        return randomNum;
    }

}

