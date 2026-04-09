package com.example.projectpoker.model;

import com.example.projectpoker.model.game.enums.Rank;

public class HandResult {
    private final int handType;
    private final int value;
    private final int kicker1;
    private final int kicker2;
    private final int kicker3;
    private final int kicker4;

    public HandResult(int handType, int value, int kicker1, int kicker2, int kicker3, int kicker4 ) {
        this.handType = handType;
        this.value = value;
        this.kicker1 = kicker1;
        this.kicker2 = kicker2;
        this.kicker3 = kicker3;
        this.kicker4 = kicker4;
    }
    // No kickers
    public HandResult(int handType, int value) {
        this(handType, value, 0, 0, 0, 0);
    }

    // 1 kicker
    public HandResult(int handType, int value, int kicker1) {
        this(handType, value, kicker1, 0, 0, 0);
    }

    // 2 kickers
    public HandResult(int handType, int value, int kicker1, int kicker2) {
        this(handType, value, kicker1, kicker2, 0, 0);
    }

    // 3 kickers
    public HandResult(int handType, int value, int kicker1, int kicker2, int kicker3) {
        this(handType, value, kicker1, kicker2, kicker3, 0);
    }


    public int getHandName() { return handType; }
    public int getValue() { return value; }
    public int getKicker1() { return kicker1; }
    public int getKicker2() { return kicker2; }
    public int getKicker3() { return kicker3; }
    public int getKicker4() { return kicker4; }

    // Call for description of hand
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
