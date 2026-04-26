package com.example.projectpoker.model.game;

import com.example.projectpoker.model.HandEvaluation;
import com.example.projectpoker.model.HandResult;
import com.example.projectpoker.model.PlayerResult;
import com.example.projectpoker.model.game.enums.PokerHand;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.projectpoker.model.game.Card.*;
import static org.junit.jupiter.api.Assertions.*;

public class HandEvalTest {

    private static ArrayList<Card> cards(Card... cards) {
        return new ArrayList<>(Arrays.asList(cards));
    }

    private static HandResult invokeEval(String methodName, ArrayList<Card> cards) {
        try {
            Method method = HandEvaluation.class.getDeclaredMethod(methodName, ArrayList.class);
            method.setAccessible(true);
            return (HandResult) method.invoke(null, cards);
        } catch (Exception e) {
            throw new RuntimeException("Failed invoking " + methodName, e);
        }
    }

    private static int invokeCompareHands(HandResult a, HandResult b) {
        try {
            Method method = HandEvaluation.class.getDeclaredMethod("compareHands", HandResult.class, HandResult.class);
            method.setAccessible(true);
            return (int) method.invoke(null, a, b);
        } catch (Exception e) {
            throw new RuntimeException("Failed invoking compareHands", e);
        }
    }

    @Test
    void whoWinsReturnsBestPlayer() {
        Player p1 = new Player("p1", 1000);
        Player p2 = new Player("p2", 1000);

        p1.addCardToHand(CA);
        p1.addCardToHand(DA);
        p2.addCardToHand(CK);
        p2.addCardToHand(DK);

        ArrayList<Card> board = cards(C2, D7, H9, S3, C4);

        ArrayList<PlayerResult> winners = HandEvaluation.whoWins(board, new ArrayList<>(Arrays.asList(p1, p2)));

        assertEquals(1, winners.size());
        assertEquals(p1.getId().getId(), winners.getFirst().getPlayerId().getId());
        assertEquals(PokerHand.ONEPAIR.getValue(), winners.getFirst().getResult().getHandName());
        assertEquals(14, winners.getFirst().getResult().getValue());
    }

    @Test
    void whoWinsReturnsBothPlayersOnTie() {
        Player p1 = new Player("p1", 1000);
        Player p2 = new Player("p2", 1000);

        p1.addCardToHand(CA);
        p1.addCardToHand(DK);
        p2.addCardToHand(HA);
        p2.addCardToHand(SK);

        ArrayList<Card> board = cards(C2, D3, H4, S5, C6);

        ArrayList<PlayerResult> winners = HandEvaluation.whoWins(board, new ArrayList<>(Arrays.asList(p1, p2)));

        assertEquals(2, winners.size());
        assertEquals(PokerHand.STRAIGHT.getValue(), winners.get(0).getResult().getHandName());
        assertEquals(PokerHand.STRAIGHT.getValue(), winners.get(1).getResult().getHandName());
        assertEquals(6, winners.get(0).getResult().getValue());
        assertEquals(6, winners.get(1).getResult().getValue());
    }

    @Test
    void compareHandsReturnsExpectedOrderAndTie() {
        HandResult better = new HandResult(PokerHand.TWOPAIR.getValue(), 14, 13, 9);
        HandResult worse = new HandResult(PokerHand.ONEPAIR.getValue(), 14, 13, 9, 7);
        HandResult sameAsBetter = new HandResult(PokerHand.TWOPAIR.getValue(), 14, 13, 9);

        assertEquals(1, invokeCompareHands(better, worse));
        assertEquals(-1, invokeCompareHands(worse, better));
        assertEquals(0, invokeCompareHands(better, sameAsBetter));
    }

    @Test
    void evaluateHandSelectsHighestRankingAvailable() {
        // Has both a pair and a straight; straight should win.
        HandResult result = invokeEval("evaluateHand", cards(C9, D8, H7, S6, C5, SA, DA));

        assertNotNull(result);
        assertEquals(PokerHand.STRAIGHT.getValue(), result.getHandName());
        assertEquals(9, result.getValue());
    }

    @Test
    void isHighCardHandlesValidAndInvalidInput() {
        HandResult result = invokeEval("isHighCard", cards(SA, DK, H9, C7, D4));

        assertNotNull(result);
        assertEquals(PokerHand.HIGHCARD.getValue(), result.getHandName());
        assertEquals(14, result.getValue());
        assertEquals(13, result.getKicker1());

        assertNull(invokeEval("isHighCard", new ArrayList<>()));
    }

    @Test
    void isPairFindsPairWithKickers() {
        HandResult result = invokeEval("isPair", cards(CQ, DQ, SA, DK, H8, C4, D2));

        assertNotNull(result);
        assertEquals(PokerHand.ONEPAIR.getValue(), result.getHandName());
        assertEquals(12, result.getValue());
        assertEquals(14, result.getKicker1());
        assertEquals(13, result.getKicker2());
        assertEquals(8, result.getKicker3());

        assertNull(invokeEval("isPair", cards(SA, DK, H9, C7, D4, C3, D2)));
    }

    @Test
    void isThreeOfAKindFindsTripsWithKickers() {
        HandResult result = invokeEval("isThreeOfAKind", cards(C7, D7, H7, SA, DK, C3, D2));

        assertNotNull(result);
        assertEquals(PokerHand.TRIPLE.getValue(), result.getHandName());
        assertEquals(7, result.getValue());
        assertEquals(14, result.getKicker1());
        assertEquals(13, result.getKicker2());

        assertNull(invokeEval("isThreeOfAKind", cards(CQ, DQ, SA, DK, H8, C4, D2)));
    }

    @Test
    void isFourOfAKindFindsQuadsWithKicker() {
        HandResult result = invokeEval("isFourOfAKind", cards(C9, D9, H9, S9, CA, D3, H2));

        assertNotNull(result);
        assertEquals(PokerHand.QUAD.getValue(), result.getHandName());
        assertEquals(9, result.getValue());
        assertEquals(14, result.getKicker1());

        assertNull(invokeEval("isFourOfAKind", cards(C7, D7, H7, SA, DK, C3, D2)));
    }

    @Test
    void isTwoPairFindsTopTwoPairsAndKicker() {
        HandResult result = invokeEval("isTwoPair", cards(CA, DA, CK, DK, H9, C2, D3));

        assertNotNull(result);
        assertEquals(PokerHand.TWOPAIR.getValue(), result.getHandName());
        assertEquals(14, result.getValue());
        assertEquals(13, result.getKicker1());
        assertEquals(9, result.getKicker2());

        assertNull(invokeEval("isTwoPair", cards(CQ, DQ, SA, DK, H8, C4, D2)));
    }

    @Test
    void isFlushFindsFlushAndReturnsTopFiveCards() {
        HandResult result = invokeEval("isFlush", cards(HA, HJ, H9, H4, H2, C3, D7));

        assertNotNull(result);
        assertEquals(PokerHand.FLUSH.getValue(), result.getHandName());
        assertEquals(14, result.getValue());
        assertEquals(11, result.getKicker1());
        assertEquals(9, result.getKicker2());
        assertEquals(4, result.getKicker3());
        assertEquals(2, result.getKicker4());

        assertNull(invokeEval("isFlush", cards(SA, DK, H9, C7, D4, C3, D2)));
    }

    @Test
    void isStraightFindsNormalAndWheelStraights() {
        HandResult normal = invokeEval("isStraight", cards(C9, D8, H7, S6, C5, DA, D2));
        assertNotNull(normal);
        assertEquals(PokerHand.STRAIGHT.getValue(), normal.getHandName());
        assertEquals(9, normal.getValue());

        HandResult wheel = invokeEval("isStraight", cards(SA, D2, C3, H4, S5, HK, D9));
        assertNotNull(wheel);
        assertEquals(PokerHand.STRAIGHT.getValue(), wheel.getHandName());
        assertEquals(5, wheel.getValue());

        assertNull(invokeEval("isStraight", cards(SA, DK, H9, C7, D4, C3, D2)));
    }

    @Test
    void isFullHouseHandlesPairAndDoubleTripleCases() {
        HandResult normal = invokeEval("isFullHouse", cards(C3, D3, H3, C2, D2, SA, HK));
        assertNotNull(normal);
        assertEquals(PokerHand.FULLHOUSE.getValue(), normal.getHandName());
        assertEquals(3, normal.getValue());
        assertEquals(2, normal.getKicker1());

        // Two triples: top triple should be main value, second triple used as pair.
        HandResult twoTriples = invokeEval("isFullHouse", cards(C7, D7, H7, C9, D9, H9));
        assertNotNull(twoTriples);
        assertEquals(PokerHand.FULLHOUSE.getValue(), twoTriples.getHandName());
        assertEquals(9, twoTriples.getValue());
        assertEquals(7, twoTriples.getKicker1());

        assertNull(invokeEval("isFullHouse", cards(C7, D7, H7, SA, DK, C3, D2)));
    }

    @Test
    void isStraightFlushDetectsStraightFlushAndRoyalFlush() {
        HandResult straightFlush = invokeEval("isStraightFlush", cards(H9, H8, H7, H6, H5, C2, D3));
        assertNotNull(straightFlush);
        assertEquals(PokerHand.STRAIGHTFLUSH.getValue(), straightFlush.getHandName());
        assertEquals(9, straightFlush.getValue());

        HandResult royalFlush = invokeEval("isStraightFlush", cards(SA, SK, SQ, SJ, ST, C2, D3));
        assertNotNull(royalFlush);
        assertEquals(PokerHand.ROYALFLUSH.getValue(), royalFlush.getHandName());
        assertEquals(14, royalFlush.getValue());

        assertNull(invokeEval("isStraightFlush", cards(HA, HJ, H9, H4, H2, C3, D7)));
    }
}
