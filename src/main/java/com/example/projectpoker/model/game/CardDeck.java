package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Rank;
import com.example.projectpoker.model.game.enums.Suit;

import java.util.ArrayList;
import java.util.Collections;

public class CardDeck {

    private ArrayList<Card> cardDeck;
    private ArrayList<Card> drawnCards;

    public CardDeck() {
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

    /**
     * Get a random card from the deck, removing it from the deck
     * @ return
     */
    public Card draw() {
        if (cardDeck.isEmpty()) { return null; }
        Card c = cardDeck.getFirst();
        cardDeck.removeFirst();
        drawnCards.add(c);
        return c;
    }

    public void burnCard() {
        Card c = cardDeck.getFirst();
        cardDeck.removeFirst();
        drawnCards.add(c);
    }
}

