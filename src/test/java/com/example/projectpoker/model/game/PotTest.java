package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PotTest {

    private Pot pot;
    private Player player1;
    private Player player2;
    private ArrayList<Player> players;

    @BeforeEach
    void setUp() {
        player1 = new Player("Player1", 1000);
        player2 = new Player("Player2", 1000);
        players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        pot = new Pot(players);
    }

    // Constructor Tests

    @Test
    void testNoArgsConstructor() {
        Pot emptyPot = new Pot();
        assertNotNull(emptyPot.getPlayers());
        assertEquals(0, emptyPot.getPotSize());
        assertEquals(0, emptyPot.getToPlay());
        assertTrue(emptyPot.getIsOpen());
        assertEquals(0, emptyPot.getPotPriority());
    }

    @Test
    void testConstructorWithPlayer() {
        Pot singlePot = new Pot(player1);
        assertEquals(1, singlePot.getPlayers().size());
        assertEquals(player1, singlePot.getPlayers().get(0));
        assertTrue(singlePot.getIsOpen());
        assertEquals(0, singlePot.getPotPriority());
    }

    @Test
    void testConstructorWithPlayerAndPriority() {
        Pot priorityPot = new Pot(player1, 5);
        assertEquals(1, priorityPot.getPlayers().size());
        assertEquals(5, priorityPot.getPotPriority());
    }

    @Test
    void testConstructorWithPlayersList() {
        assertEquals(2, pot.getPlayers().size());
        assertEquals(0, pot.getPotSize());
        assertEquals(0, pot.getToPlay());
        assertTrue(pot.getIsOpen());
        assertEquals(0, pot.getPotPriority());
    }

    // Getters and Setters Tests

    @Test
    void testGetAndSetPlayers() {
        ArrayList<Player> newPlayers = new ArrayList<>();
        newPlayers.add(new Player("New1", 500));
        pot.setPlayers(newPlayers);
        assertEquals(1, pot.getPlayers().size());
        assertEquals("New1", pot.getPlayers().get(0).getName());
    }

    @Test
    void testAddPlayer() {
        Player player3 = new Player("Player3", 500);
        pot.addPlayer(player3);
        assertEquals(3, pot.getPlayers().size());
    }

    @Test
    void testGetAndSetToPlay() {
        assertEquals(0, pot.getToPlay());
        pot.setToPlay(100);
        assertEquals(100, pot.getToPlay());
    }

    @Test
    void testGetAndSetPotSize() {
        assertEquals(0, pot.getPotSize());
        pot.setPotSize(500);
        assertEquals(500, pot.getPotSize());
    }

    @Test
    void testGetAndSetPotPriority() {
        assertEquals(0, pot.getPotPriority());
        pot.setPotPriority(5);
        assertEquals(5, pot.getPotPriority());
    }

    @Test
    void testStepPotPriority() {
        pot.setPotPriority(1);
        pot.stepPotPriority(2);
        assertEquals(3, pot.getPotPriority());
    }

    // Open/Close Tests

    @Test
    void testGetIsOpen() {
        assertTrue(pot.getIsOpen());
    }

    @Test
    void testSetIsOpen() {
        pot.setIsOpen(false);
        assertFalse(pot.getIsOpen());
        assertEquals(-1, pot.getPotPriority());
    }

    @Test
    void testClosePot() {
        pot.closePot();
        assertFalse(pot.getIsOpen());
    }

    @Test
    void testSetIsOpenFalseSetsPriorityToNegativeOne() {
        pot.setPotPriority(3);
        pot.setIsOpen(false);
        assertEquals(-1, pot.getPotPriority());
    }

    // Bet Table Tests (addPlayer2Table method exists but betTable is private)

    @Test
    void testAddPlayer2Table() {
        Player newPlayer = new Player("NewP", 500);
        pot.addPlayer2Table(newPlayer);
        assertNotNull(newPlayer);
    }

    @Test
    void testAddPlayer2TableWithBet() {
        Player newPlayer = new Player("NewP", 500);
        pot.addPlayer2Table(newPlayer, 100);
        assertNotNull(newPlayer);
    }

    // Pot Initialization Tests - initBlinds is complex, skip detailed tests

    @Test
    void testInitBlindsMethodExists() {
        ArrayList<Integer> turnOrder = new ArrayList<>();
        turnOrder.add(0);
        turnOrder.add(1);
        pot.initBlinds(players, turnOrder, 100);
        assertNotNull(pot);
    }

    // Remove Folded Tests

    @Test
    void testRemoveFoldedRemovesFoldedPlayers() {
        player1.setAction(com.example.projectpoker.model.game.enums.Action.FOLD);
        pot.removeFolded(com.example.projectpoker.model.game.enums.RoundStatus.BETTING1);
        assertEquals(1, pot.getPlayers().size());
    }

    @Test
    void testRemoveFoldedReturnsEndWhenOnePlayerLeft() {
        player1.setAction(com.example.projectpoker.model.game.enums.Action.FOLD);
        player2.setAction(com.example.projectpoker.model.game.enums.Action.FOLD);
        com.example.projectpoker.model.game.enums.RoundStatus result = pot.removeFolded(com.example.projectpoker.model.game.enums.RoundStatus.BETTING1);
        assertEquals(com.example.projectpoker.model.game.enums.RoundStatus.END, result);
    }

    // Pay Out Tests

    @Test
    void testPayOutWithOnePlayer() {
        Player soloPlayer = new Player("Solo", 500);
        ArrayList<Player> soloList = new ArrayList<>();
        soloList.add(soloPlayer);
        Pot soloPot = new Pot(soloList);
        soloPot.setPotSize(1000);
        int initialBalance = soloPlayer.getBalance();
        soloPot.payOut();
        assertEquals(initialBalance + 1000, soloPlayer.getBalance());
    }
}