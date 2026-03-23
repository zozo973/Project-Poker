package com.example.projectpoker.GameLogic;

import java.util.*;

public class Board {
    private List<Card> communityCards = new ArrayList<>();

    public void addCard(Card card) {
        if (communityCards.size() >= 5) {
            throw new IllegalStateException("Board already has 5 cards");
        }
        communityCards.add(card);
    }

    public List<Card> getCards() {
        return communityCards;
    }

    public void clear() {
        communityCards.clear();
    }

    @Override
    public String toString() {
        return communityCards.toString();
    }
}