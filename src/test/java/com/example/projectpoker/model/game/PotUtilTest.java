package com.example.projectpoker.model.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PotUtilTest {

    private ArrayList<Pot> pots;
    private Player player1;
    private Player player2;
    private Player player3;

    @BeforeEach
    void setUp() {
        player1 = new Player("Player1", 1000);
        player2 = new Player("Player2", 1000);
        player3 = new Player("Player3", 1000);
        
        pots = new ArrayList<>();
        pots.add(new Pot(player1));
        pots.add(new Pot(player2));
    }

    // Add New Side Pot Tests

    @Test
    void testAddNewSidePotCreatesNewPot() {
        int initialSize = pots.size();
        player1.placeBet(200, pots.get(0));
        ArrayList<Pot> result = PotUtil.addNewSidePot(pots, player1);
        assertTrue(result.size() > initialSize);
    }

    @Test
    void testAddNewSidePotUpdatesPriority() {
        player1.setRole(com.example.projectpoker.model.game.enums.Roles.BIGBLIND);
        player2.setRole(com.example.projectpoker.model.game.enums.Roles.PLAYER);
        player1.placeBet(200, pots.get(0));
        ArrayList<Pot> result = PotUtil.addNewSidePot(pots, player1);
        assertNotNull(result);
    }

    // Try Pay Multiple Pots Tests

    @Test
    void testTryPayMultiplePotsWithOneOpenPot() {
        ArrayList<Pot> result = PotUtil.tryPayMultiplePots(pots, player1);
        assertNull(result);
    }

    @Test
    void testTryPayMultiplePotsWithMultipleOpenPots() {
        pots.clear();
        Pot openPot1 = new Pot(player1);
        openPot1.setIsOpen(true);
        Pot openPot2 = new Pot(player2);
        openPot2.setIsOpen(true);
        
        ArrayList<Pot> multiPots = new ArrayList<>();
        multiPots.add(openPot1);
        multiPots.add(openPot2);
        
        ArrayList<Pot> result = PotUtil.tryPayMultiplePots(multiPots, player1);
        assertNotNull(result);
    }

    // Pay Multiple Side Pots Tests

    @Test
    void testPayMultipleSidePots() {
        player1.placeBet(100, pots.get(0));
        player2.placeBet(50, pots.get(1));
        
        ArrayList<Pot> result = PotUtil.payMultipleSidePots(pots, player1);
        assertNotNull(result);
    }

    // Find Highest Priority Pot Tests

    @Test
    void testFindHighestPriorityPot() {
        pots.get(0).setPotPriority(2);
        pots.get(1).setPotPriority(1);
        
        Pot result = PotUtil.findHighestPriorityPot(pots);
        
        assertEquals(1, result.getPotPriority());
    }

    @Test
    void testFindHighestPriorityPotReturnsFirstMatch() {
        pots.get(0).setPotPriority(0);
        pots.get(1).setPotPriority(0);
        
        Pot result = PotUtil.findHighestPriorityPot(pots);
        
        assertNotNull(result);
        assertEquals(0, result.getPotPriority());
    }

    // Pay Open Pot Tests

    @Test
    void testPayOpenPot() {
        player1.placeBet(100, pots.get(0));
        
        ArrayList<Pot> result = PotUtil.payOpenPot(pots, player1);
        
        assertNotNull(result);
    }

    @Test
    void testPayOpenPotThrowsWhenNoOpenPot() {
        pots.clear();
        pots.add(new Pot(player1));
        pots.get(0).setIsOpen(false);
        
        assertThrows(IllegalStateException.class, () -> PotUtil.payOpenPot(pots, player1));
    }

    // Get Open Pot Index Tests

    @Test
    void testGetOpenPotIndexWithSinglePot() {
        int index = PotUtil.getOpenPotIndex(pots);
        assertEquals(0, index);
    }

    @Test
    void testGetOpenPotIndexWithMultiplePots() {
        Pot closedPot = new Pot(player3);
        closedPot.setIsOpen(false);
        pots.add(closedPot);
        pots.add(new Pot(player3));
        
        int index = PotUtil.getOpenPotIndex(pots);
        assertTrue(index >= 0);
    }

    @Test
    void testGetOpenPotIndexReturnsNegativeOneWhenNoOpenPot() {
        for (Pot pot : pots) {
            pot.setIsOpen(false);
        }
        
        int index = PotUtil.getOpenPotIndex(pots);
        assertEquals(-1, index);
    }

    // Edge Cases

    @Test
    void testGetOpenPotIndexWithEmptyList() {
        ArrayList<Pot> emptyPots = new ArrayList<>();
        int index = PotUtil.getOpenPotIndex(emptyPots);
        assertEquals(-1, index);
    }

    @Test
    void testFindHighestPriorityPotWithEmptyList() {
        ArrayList<Pot> emptyPots = new ArrayList<>();
        Pot result = PotUtil.findHighestPriorityPot(emptyPots);
        assertNull(result);
    }
}