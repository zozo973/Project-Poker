package com.example.projectpoker.model.game;

public class BetTypeLogic {

    private static BetType selectBetType() {
        return BetType.SIDEPOT;
    }

    public static void executeBets(BetType betType) {
        switch(betType) {
            case BetType.NORMAL:
                normalBet();
            case BetType.SKIP2SHOWDOWN:
                skipBet();
            case BetType.SIDEPOT:
                sidePotBet();
            case BetType.ENDROUND:
                endRound();
        }
    }

    public static void normalBet() {

    }

    public static void skipBet() {

    }

    public static void sidePotBet() {

    }

    public static void endRound() {

    }
}
