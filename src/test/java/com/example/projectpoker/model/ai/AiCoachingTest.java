package com.example.projectpoker.model.ai;

import com.example.projectpoker.AiCoaching;
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.enums.AiAdviceMode;
import com.example.projectpoker.model.game.enums.RoundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.projectpoker.model.game.Card.*;
import static org.junit.jupiter.api.Assertions.*;


public class AiCoachingTest {

    private AiCoaching coach;

    @BeforeEach
    void setUp() {
        coach = new AiCoaching();
    }

    // Test 1: test Null situation
    @Test
    void testNullHandReturnsError() {
        AiCoaching.AiAdvice advice = coach.getAdvice(null, null, RoundStatus.BETTING1, AiAdviceMode.NORMAL);
        assertNotNull(advice.errormsg);
    }

    // Test 2: test the situation when user get the great cards
    @Test
    void testStrongHandRiskyModeReturnsValidAdvice() {
        Card[] hand  = {SA, HA};
        Card[] board = {C2, D5, S9};
        AiCoaching.AiAdvice advice = coach.getAdvice(hand, board, RoundStatus.FLOP, AiAdviceMode.RISKY);
        assertNull(advice.errormsg);
        assertNotNull(advice.action);
        assertTrue(advice.confidence >= 0 && advice.confidence <= 100);
        assertNotNull(advice.reason);
    }

    // Test 3: test the situation when user get the bad cards
    @Test
    void testWeakHandSafeModeReturnsValidAdvice() {
        Card[] hand  = {H2, C7};
        Card[] board = {DA, HK, SQ, CJ};
        AiCoaching.AiAdvice advice = coach.getAdvice(hand, board, RoundStatus.TURN, AiAdviceMode.SAFE);
        assertNull(advice.errormsg);
        assertNotNull(advice.action);
        assertNotNull(advice.reason);
    }

}
