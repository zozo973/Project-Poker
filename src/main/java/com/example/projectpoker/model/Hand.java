package com.example.projectpoker.model;
import com.example.projectpoker.model.game.Card;

import java.util.*;

public class Hand {
    public List<Card> cards;

    public Hand() {
        cards = new ArrayList<>();
    }
    public void addCard(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return cards;
    }

    public void clear() {
        cards.clear();
    }

    @Override
    public String toString() {
        return cards.toString();
    }
}