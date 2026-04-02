package com.example.projectpoker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;


public class HandEvaluation {

    public static List<PlayerResult> whoWins(List<Card> board, List<List<Card>> playerHands) {

        List<PlayerResult> winners = new ArrayList<>();
        HandResult bestResult = null;

        for (int i = 0; i < playerHands.size(); i++) {

            List<Card> combined = new ArrayList<>(board);
            combined.addAll(playerHands.get(i));

            HandResult currentResult = HandEvaluation.evaluateHand(combined);

            if (bestResult == null) {
                bestResult = currentResult;
                winners.add(new PlayerResult(playerHands.get(i), currentResult));
            } else {
                int cmp = compareHands(currentResult, bestResult);

                if (cmp > 0) {
                    // New best → clear old winners
                    winners.clear();
                    winners.add(new PlayerResult(playerHands.get(i), currentResult));
                    bestResult = currentResult;

                } else if (cmp == 0) {
                    // Tie → add to winners
                    winners.add(new PlayerResult(playerHands.get(i), currentResult));
                }
            }
        }

        return winners;
    }

    private static int compareHands(HandResult a, HandResult b) {

        int[] aVals = {
                a.getHandName(),
                a.getValue(),
                a.getKicker1(),
                a.getKicker2(),
                a.getKicker3(),
                a.getKicker4()
        };

        int[] bVals = {
                b.getHandName(),
                b.getValue(),
                b.getKicker1(),
                b.getKicker2(),
                b.getKicker3(),
                b.getKicker4()
        };

        for (int i = 0; i < aVals.length; i++) {
            if (aVals[i] > bVals[i]) return 1;
            if (aVals[i] < bVals[i]) return -1;
        }

        return 0; // tie
    }

    public static HandResult evaluateHand(List<Card> cards) {
        HandResult testEvaluation;

        testEvaluation = isStraightFlush(cards);
        if (testEvaluation !=null) return testEvaluation;

        testEvaluation = isFourOfAKind(cards);
        if (testEvaluation !=null) return testEvaluation;

        testEvaluation = isFullHouse(cards);
        if (testEvaluation !=null) return testEvaluation;

        testEvaluation = isFlush(cards);
        if (testEvaluation !=null) return testEvaluation;

        testEvaluation = isStraight(cards);
        if (testEvaluation !=null) return testEvaluation;

        testEvaluation = isThreeOfAKind(cards);
        if (testEvaluation !=null) return testEvaluation;

        testEvaluation = isTwoPair(cards);
        if (testEvaluation !=null) return testEvaluation;

        testEvaluation = isPair(cards);
        if (testEvaluation !=null) return testEvaluation;

        testEvaluation = isHighCard(cards);
        return testEvaluation;


    }

    private static HandResult isHighCard(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return null;

        // Extract values
        List<Integer> values = new ArrayList<>();
        for (Card card : cards) {
            values.add(card.getValue());
        }

        // Sort descending
        values.sort(Collections.reverseOrder());

        int value = values.get(0);
        int kicker1 = values.size() > 1 ? values.get(1) : 0;
        int kicker2 = values.size() > 2 ? values.get(2) : 0;
        int kicker3 = values.size() > 3 ? values.get(3) : 0;
        int kicker4 = values.size() > 4 ? values.get(4) : 0;

        return new HandResult(PokerHand.HIGHCARD.getValue(), value, kicker1, kicker2, kicker3, kicker4);
    }

    private static HandResult isPair(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            int count = 1;
            int rank = cards.get(i).getValue();

            for (int j = i + 1; j < cards.size(); j++) {
                if (rank == cards.get(j).getValue()) {
                    count++;
                }
            }

            if (count == 2) {
                int kicker1 = -1;
                int kicker2 = -1;
                int kicker3 = -1;

                // find the Highest kicker card
                for (Card card : cards) {
                    if (card.getValue() != rank) {
                        kicker1 = Math.max(kicker1, card.getValue());
                    }
                }
                // find the 2nd kicker card
                for (Card card : cards) {
                    if (card.getValue() != rank && card.getValue() != kicker1) {
                        kicker2 = Math.max(kicker2, card.getValue());
                    }
                }
                // find the 3rd kicker card
                for (Card card : cards) {
                    if (card.getValue() != rank&& card.getValue() != kicker1 && card.getValue() != kicker2) {
                        kicker3 = Math.max(kicker3, card.getValue());
                    }
                }


                return new HandResult(PokerHand.ONEPAIR.getValue(), rank, kicker1, kicker2, kicker3);
            }
        }

        return null; // no pairs
    }

    private static HandResult isThreeOfAKind(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            int count = 1;
            int rank = cards.get(i).getValue();

            for (int j = i + 1; j < cards.size(); j++) {
                if (rank == cards.get(j).getValue()) {
                    count++;
                }
            }

            if (count >= 3) {
                int kicker1 = -1;
                int kicker2 = -1;

                // find the Highest kicker card
                for (Card card : cards) {
                    if (card.getValue() != rank) {
                        kicker1 = Math.max(kicker1, card.getValue());
                    }
                }
                // find the 2nd kicker card
                for (Card card : cards) {
                    if (card.getValue() != rank && card.getValue() != kicker1) {
                        kicker2 = Math.max(kicker2, card.getValue());
                    }
                }
                return new HandResult(PokerHand.TRIPLE.getValue(), rank, kicker1, kicker2);
            }
        }

        return null; // no triple
    }

    private static HandResult isFourOfAKind(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            int count = 1;
            int rank = cards.get(i).getValue();

            for (int j = i + 1; j < cards.size(); j++) {
                if (rank == cards.get(j).getValue()) {
                    count++;
                }
            }

            if (count >= 4) {
                int kicker = -1;

                // find the Highest kicker card
                for (Card card : cards) {
                    if (card.getValue() != rank) {
                        kicker = Math.max(kicker, card.getValue());
                    }
                }

                return new HandResult(PokerHand.QUAD.getValue(), rank, kicker);
            }
        }

        return null; // no four of a kind
    }

    private static HandResult isTwoPair(List<Card> cards) {
        Map<Integer, Integer> counts = new HashMap<>();

        // Count occurrences of each value
        for (Card card : cards) {
            int value = card.getValue();
            counts.put(value, counts.getOrDefault(value, 0) + 1);
        }

        // Collect all pairs
        List<Integer> pairs = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= 2) {
                pairs.add(entry.getKey());
            }
        }

        // Need at least 2 pairs
        if (pairs.size() < 2) return null;

        // Sort pairs descending
        pairs.sort(Collections.reverseOrder());

        int highPair = pairs.get(0);
        int secondPair = pairs.get(1);

        // Find highest kicker not part of the pairs
        int kicker = -1;
        for (Card card : cards) {
            int value = card.getValue();
            if (value != highPair && value != secondPair) {
                kicker = Math.max(kicker, value);
            }
        }

        return new HandResult(PokerHand.TWOPAIR.getValue(), highPair, secondPair, kicker);
    }

    private static HandResult isFlush(List<Card> cards) {
        Map<Suit, List<Integer>> suitMap = new HashMap<>();

        // Group card values by suit
        for (Card card : cards) {
            Suit suit = card.getSuit();
            suitMap.putIfAbsent(suit, new ArrayList<>());
            suitMap.get(suit).add(card.getValue());
        }

        // Check each suit
        for (List<Integer> values : suitMap.values()) {
            if (values.size() >= 5) {

                // Sort descending
                values.sort(Collections.reverseOrder());

                int value = values.get(0);
                int kicker1 = values.size() > 1 ? values.get(1) : 0;
                int kicker2 = values.size() > 2 ? values.get(2) : 0;
                int kicker3 = values.size() > 3 ? values.get(3) : 0;
                int kicker4 = values.size() > 4 ? values.get(4) : 0;

                return new HandResult(PokerHand.FLUSH.getValue(), value, kicker1, kicker2, kicker3, kicker4);
            }
        }

        return null;
    }

    private static HandResult isStraight(List<Card> cards) {
        if (cards.size() < 5) return null;

        Set<Integer> uniqueValues = new HashSet<>();
        for (Card card : cards) {
            uniqueValues.add(card.getValue());
        }

        // Treat Ace as low (1) as well
        if (uniqueValues.contains(14)) {
            uniqueValues.add(1);
        }

        List<Integer> sorted = new ArrayList<>(uniqueValues);
        Collections.sort(sorted);

        int streak = 1;
        int bestHighCard = 0;

        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i) == sorted.get(i - 1) + 1) {
                streak++;

                if (streak >= 5) {
                    bestHighCard = sorted.get(i); // keep updating!
                }
            } else {
                streak = 1;
            }
        }

        if (bestHighCard > 0) {
            return new HandResult(PokerHand.STRAIGHT.getValue(), bestHighCard);
        }

        return null;
    }

    private static HandResult isFullHouse(List<Card> cards) {
        Map<Integer, Integer> counts = new HashMap<>();

        // Count occurrences
        for (Card card : cards) {
            int value = card.getValue();
            counts.put(value, counts.getOrDefault(value, 0) + 1);
        }

        List<Integer> triples = new ArrayList<>();
        List<Integer> pairs = new ArrayList<>();

        // Separate triples and pairs
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue();

            if (count >= 3) {
                triples.add(value);
            } else if (count >= 2) {
                pairs.add(value);
            }
        }

        if (triples.isEmpty()) return null;

        // Sort descending
        triples.sort(Collections.reverseOrder());
        pairs.sort(Collections.reverseOrder());

        int tripleValue = triples.get(0);

        int pairValue = 0;

        // Case 1: another pair exists
        if (!pairs.isEmpty()) {
            pairValue = pairs.get(0);
        }
        // Case 2: second triple becomes the pair
        else if (triples.size() > 1) {
            pairValue = triples.get(1);
        } else {
            return null;
        }

        return new HandResult(PokerHand.FULLHOUSE.getValue(), tripleValue, pairValue);
    }

    private static HandResult isStraightFlush(List<Card> cards) {
        Map<Suit, List<Integer>> suitMap = new EnumMap<>(Suit.class);

        // Group values by suit
        for (Card card : cards) {
            suitMap.putIfAbsent(card.getSuit(), new ArrayList<>());
            suitMap.get(card.getSuit()).add(card.getValue());
        }

        int bestHighCard = 0;

        // Check each suit separately
        for (List<Integer> values : suitMap.values()) {
            if (values.size() < 5) continue;

            // Remove duplicates
            Set<Integer> uniqueValues = new HashSet<>(values);

            // Ace can be low
            if (uniqueValues.contains(14)) {
                uniqueValues.add(1);
            }

            List<Integer> sorted = new ArrayList<>(uniqueValues);
            Collections.sort(sorted);

            int streak = 1;

            for (int i = 1; i < sorted.size(); i++) {
                if (sorted.get(i) == sorted.get(i - 1) + 1) {
                    streak++;

                    if (streak >= 5) {
                        bestHighCard = Math.max(bestHighCard, sorted.get(i));
                    }
                } else {
                    streak = 1;
                }
            }
        }

        if (bestHighCard == 0) return null;

        // Royal Flush check
        if (bestHighCard == 14) {
            return new HandResult(PokerHand.ROYALFLUSH.getValue(), 14); // Royal Flush
        }

        return new HandResult(PokerHand.STRAIGHTFLUSH.getValue(), bestHighCard); // Straight Flush
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