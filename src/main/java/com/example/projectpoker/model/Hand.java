package com.example.projectpoker.model;


import java.util.*;

public class Hand {
    private List<Card> cards = new ArrayList<>();

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