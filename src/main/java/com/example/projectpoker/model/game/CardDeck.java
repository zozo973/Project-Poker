package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Rank;
import com.example.projectpoker.model.game.enums.Suit;

import java.util.ArrayList;
import java.util.Collections;

public class CardDeck {

    private final ArrayList<Card> cardDeck;

    public CardDeck() {
        cardDeck = new ArrayList<>(52);
        reset();
    }

    public void shuffle() {
        Collections.shuffle(this.cardDeck);
    }

    public void reset() {
        cardDeck.clear();
        for (Suit s : Suit.values()) {
            for (Rank r : Rank.values()) {
                Card c = new Card(s,r);
                cardDeck.add(c);
            }
        }
        shuffle();
    }

    /**
     * Get a random card from the deck, removing it from the deck
     * @ return
     */
    public Card draw() {
        if (cardDeck.isEmpty()) { return null; }
        Card c = cardDeck.getFirst();
        cardDeck.removeFirst();
        return c;
    }

    public void burnCard() {
        cardDeck.removeFirst();

    }
}

