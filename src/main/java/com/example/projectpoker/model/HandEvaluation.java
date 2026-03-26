package com.example.projectpoker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HandEvaluation {


    private static PokerHand evaluateHand(List<Card> cards) {

        if (isRoyalFlush(cards)) return (PokerHand.ROYALFLUSH);
        if (isStraightFlush(cards)) return PokerHand.STRAIGHTFLUSH;
        if (isFourOfAKind(cards)) return PokerHand.QUAD;
        if (isFullHouse(cards)) return PokerHand.FULLHOUSE;
        if (isFlush(cards)) return PokerHand.FLUSH;
        if (isStraight(cards)) return PokerHand.STRAIGHT;
        if (isThreeOfAKind(cards)) return PokerHand.TRIPLE;
        if (isTwoPair(cards)) return PokerHand.TWOPAIR;
        if (isPair(cards)) return PokerHand.ONEPAIR;
        return PokerHand.HIGHCARD;
    }

    private static boolean isPair(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            int count = 1;

            for (int j = i + 1; j < cards.size(); j++) {
                if (cards.get(i).getRank() == cards.get(j).getRank()) {
                    count++;
                }
            }

            if (count >= 2) return true;
        }
        return false;
    }

    private static boolean isThreeOfAKind(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            int count = 1;

            for (int j = i + 1; j < cards.size(); j++) {
                if (cards.get(i).getRank() == cards.get(j).getRank()) {
                    count++;
                }
            }

            if (count >= 3) return true;
        }
        return false;
    }

    private static boolean isFourOfAKind(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            int count = 1;

            for (int j = i + 1; j < cards.size(); j++) {
                if (cards.get(i).getRank() == cards.get(j).getRank()) {
                    count++;
                }
            }

            if (count >= 4) return true;
        }
        return false;
    }

    private static boolean isTwoPair(List<Card> cards) {
        int pairs = 0;

        for (int i = 0; i < cards.size(); i++) {
            int count = 1;

            for (int j = i + 1; j < cards.size(); j++) {
                if (cards.get(i).getRank() == cards.get(j).getRank()) {
                    count++;
                }
            }

            if (count >= 2) {
                pairs++;
                i++;
            }
        }

        return pairs >= 2;
    }

    private static boolean isFlush(List<Card> cards){
        for (int i = 0; i < cards.size(); i++) {
            int count = 1;

            for (int j = i + 1; j < cards.size(); j++) {
                if (cards.get(i).getSuit() == cards.get(j).getSuit()) {
                    count++;
                }
            }

            if (count >= 5) return true;
        }
        return false;
    }

    private static boolean isStraight(List<Card> cards){
        if (cards.size() < 5) return false;

        // collect unique values
        Set<Integer> uniqueValues = new HashSet<>();
        for (Card card : cards) {
            uniqueValues.add(card.getValue());
        }
        // edge case where ace is 1
        if (uniqueValues.contains(14) && uniqueValues.contains(2) && uniqueValues.contains(3) && uniqueValues.contains(4) && uniqueValues.contains(5)) {
            return true;
        }

        // sort values
        List<Integer> sorted = new ArrayList<>(uniqueValues);
        Collections.sort(sorted);

        // look for streak of 5
        int streak = 1;

        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i) == sorted.get(i - 1) + 1) {
                streak++;
                if (streak >= 5) return true;
            } else {
                streak = 1;
            }
        }

        return false;
    }

    //Implement these methods
    private static boolean isFullHouse(List<Card> cards){
        return false;
    }
    private static boolean isStraightFlush(List<Card> cards){
        return false;
    }
    private static boolean isRoyalFlush(List<Card> cards){
        return false;
    }


    private static List<Integer> getRanks(List<Card> cards) {
        List<Integer> returnList = new ArrayList<>();

        for (Card card : cards) {
            returnList.add(card.getValue());
        }

        return returnList;
    }
    private static List<Suit> getSuits(List<Card> cards) {
        List<Suit> returnList = new ArrayList<>();

        for (Card card : cards) {
            returnList.add(card.getSuit());
        }

        return returnList;
    }


}