package com.example.projectpoker.model;
import com.example.projectpoker.model.game.Card;

import java.util.*;

public class Hand {
    public List<Card> cards;

    /**
     * Creates an empty hand.
     */
    public Hand() {
        cards = new ArrayList<>();
    }

    /**
     * Adds a card to this hand.
     *
     * @param card the {@link Card} to add
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Returns the list of cards currently held in this hand.
     *
     * @return the {@link List} of {@link Card} objects
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Removes all cards from this hand, leaving it empty.
     */
    public void clear() {
        cards.clear();
    }

    /**
     * Returns a string representation of the cards in this hand.
     *
     * @return a string listing the cards, e.g. "[Ace of Hearts, King of Spades]"
     */
    @Override
    public String toString() {
        return cards.toString();
    }
}