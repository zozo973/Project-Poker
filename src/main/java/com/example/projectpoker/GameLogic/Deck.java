package com.example.projectpoker.GameLogic;

import java.util.*;

public class Deck {
    private List<Card> cards;
    private Random random = new Random();

    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
    }

    private void initializeDeck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty");
        }
        int index = random.nextInt(cards.size());
        return cards.remove(index);
    }
}