package com.example.projectpoker.model.statistics;

// Used to calculate hand statistics but does not form part of final product.




import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.CardDeck;
import com.example.projectpoker.model.HandEvaluation;
import com.example.projectpoker.model.game.enums.PokerHand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandStats {

public static void CalcStats(int tests, int handsize) {
    Map<Integer, Integer> stats = new HashMap<>();

    for (int i = 0; i < tests; i++) {
        CardDeck deck = new CardDeck();
        deck.shuffle();
        ArrayList<Card> Cards = new ArrayList<>();

        for (int j = 0; j < handsize; j++) {
            Cards.add(deck.draw());
        }

        int handType = HandEvaluation.evaluateHand(Cards).getHandName();

        stats.put(handType, stats.getOrDefault(handType, 0) + 1);
    }

    System.out.println("Hand Type        Count       Percentage");
    System.out.println("----------------------------------------");

    for (
            PokerHand hand : PokerHand.values()) {
        int count = stats.getOrDefault(hand.getValue(), 0);
        double pct = (count * 100.0) / tests;

        System.out.printf("%-15s %-10d %.4f%%%n",
                hand.name(), count, pct);
    }
}
}
