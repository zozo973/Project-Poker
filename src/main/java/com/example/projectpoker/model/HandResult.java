package com.example.projectpoker.model;

import com.example.projectpoker.model.game.enums.Rank;

public class HandResult {
    private final int handType;
    private final int value;
    private final int kicker1;
    private final int kicker2;
    private final int kicker3;
    private final int kicker4;

    /**
     * Creates a complete hand result with all kicker values.
     *
     * @param handType type identifier for the poker hand (1-10, where 10 is a royal flush)
     * @param value primary hand strength value (typically 2-14 for card ranks)
     * @param kicker1 first tiebreaker card value
     * @param kicker2 second tiebreaker card value
     * @param kicker3 third tiebreaker card value
     * @param kicker4 fourth tiebreaker card value
     */
    public HandResult(int handType, int value, int kicker1, int kicker2, int kicker3, int kicker4 ) {
        this.handType = handType;
        this.value = value;
        this.kicker1 = kicker1;
        this.kicker2 = kicker2;
        this.kicker3 = kicker3;
        this.kicker4 = kicker4;
    }
    // No kickers
    /**
     * Creates a hand result with no kickers.
     *
     * @param handType type identifier for the poker hand
     * @param value primary hand strength value
     */
    public HandResult(int handType, int value) {
        this(handType, value, 0, 0, 0, 0);
    }

    // 1 kicker
    /**
     * Creates a hand result with one kicker.
     *
     * @param handType type identifier for the poker hand
     * @param value primary hand strength value
     * @param kicker1 first tiebreaker card value
     */
    public HandResult(int handType, int value, int kicker1) {
        this(handType, value, kicker1, 0, 0, 0);
    }

    // 2 kickers
    /**
     * Creates a hand result with two kickers.
     *
     * @param handType type identifier for the poker hand
     * @param value primary hand strength value
     * @param kicker1 first tiebreaker card value
     * @param kicker2 second tiebreaker card value
     */
    public HandResult(int handType, int value, int kicker1, int kicker2) {
        this(handType, value, kicker1, kicker2, 0, 0);
    }

    // 3 kickers
    /**
     * Creates a hand result with three kickers.
     *
     * @param handType type identifier for the poker hand
     * @param value primary hand strength value
     * @param kicker1 first tiebreaker card value
     * @param kicker2 second tiebreaker card value
     * @param kicker3 third tiebreaker card value
     */
    public HandResult(int handType, int value, int kicker1, int kicker2, int kicker3) {
        this(handType, value, kicker1, kicker2, kicker3, 0);
    }

    /**
     * Returns the numeric hand type, where higher is better (1 = high card, 10 = royal flush).
     *
     * @return the hand type ordinal value
     */
    public int getHandName() { return handType; }

    /**
     * Returns the primary rank value used when comparing two hands of the same type.
     * For example, the rank of the pair in a one-pair hand, or the high card in a flush.
     *
     * @return the primary hand value integer (2–14)
     */
    public int getValue() { return value; }

    /**
     * Returns the value of the first kicker card used to break ties.
     *
     * @return the first kicker value, or {@code 0} if not applicable
     */
    public int getKicker1() { return kicker1; }

    /**
     * Returns the value of the second kicker card used to break ties.
     *
     * @return the second kicker value, or {@code 0} if not applicable
     */
    public int getKicker2() { return kicker2; }

    /**
     * Returns the value of the third kicker card used to break ties.
     *
     * @return the third kicker value, or {@code 0} if not applicable
     */
    public int getKicker3() { return kicker3; }

    /**
     * Returns the value of the fourth kicker card used to break ties.
     *
     * @return the fourth kicker value, or {@code 0} if not applicable
     */
    public int getKicker4() { return kicker4; }

    /**
     * Returns a human-readable description of this hand result
     * (e.g. "Royal Flush", "Pair of Aces", "Two pairs: Kings and Jacks").
     *
     * @return descriptive string of the hand
     */
    public @Override String toString()
    {
        switch (handType) {
            //Royal Flush
            case 10:
                return "IT'S A ROYAL FLUSH!!";
            //Straight Flush
            case 9:
                if (Rank.values()[value - 2] == Rank.Five) {
                    return "It's a Straight Flush: Ace through to 5.";
                }
                return "It's a Straight Flush: " + Rank.values()[value-6] + " through to " + Rank.values()[value-2] + ".";
            // Four of a kind
            case 8:
                return "Four of a kind: "+ Rank.values()[value-2] + "s.";
            // Full house
            case 7:
                return "Full house: " + Rank.values()[value-2] +"s full of "+ Rank.values()[kicker1-2] + "s.";
            // Flush
            case 6:
                return "Its a flush";
            // Straight
            case 5:
                if (Rank.values()[value - 2] == Rank.Five) {
                    return "It's a Straight: Ace through to 5.";
                }

                return "It's a Straight: " + Rank.values()[value-6] + " through to " + Rank.values()[value-2] + ".";
            // Three of a kind
            case 4:
                return "Three of a kind: "+ Rank.values()[value-2] + "s.";
            // Two Pair
            case 3:
                return "Two pairs: "+ Rank.values()[value-2] + "s and "+ Rank.values()[kicker1-2] + "s." ;
            // Pair
            case 2:
                return "Pair of " + Rank.values()[value-2] + "s.";
            //
            case 1:
                return "High Card: " + Rank.values()[value-2] + ".";


            default:
                return "";


        }
    }
}
