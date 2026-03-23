package com.example.projectpoker.model;

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
        // int drawnIndex = randInt(0,cardDeck.size()-1);
        Card c = cardDeck.getFirst();
        cardDeck.removeFirst();
        drawnCards.add(c);
        return c;
    }
}

