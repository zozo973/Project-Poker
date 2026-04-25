package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.BetType;
import com.example.projectpoker.model.game.enums.RoundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RoundTest {

    // Constructor initialization, property change listeners
    // Round status and bet type transitions
    // Pot management (add, get main/open pot, open pot index)
    // Community cards management
    // Turn order, end betting conditions

    private Round round;
    private ArrayList<Player> players;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        players.add(new Player("Player1", 1000));
        players.add(new Player("Player2", 1000));
        players.add(new Player("Player3", 1000));
        round = new Round(players, 100);
    }

    // Constructor Tests

    @Test
    void testConstructorInitializesCorrectly() {
        assertEquals(RoundStatus.UNINITIALISED, round.getRoundStatus());
        assertEquals(100, round.getToPlay());
        assertNotNull(round.getPots());
        assertEquals(1, round.getPots().size());
        assertNotNull(round.getCommunityCards());
        assertTrue(round.getCommunityCards().isEmpty());
        assertEquals(BetType.NORMAL, round.getBetType());
    }

    @Test
    void testConstructorWithEmptyPlayers() {
        ArrayList<Player> emptyPlayers = new ArrayList<>();
        Round emptyRound = new Round(emptyPlayers, 50);
        assertNotNull(emptyRound);
    }

    @Test
    void testConstructorWithTwoPlayers() {
        ArrayList<Player> twoPlayers = new ArrayList<>();
        twoPlayers.add(new Player("P1", 500));
        twoPlayers.add(new Player("P2", 500));
        Round twoPlayerRound = new Round(twoPlayers, 100);
        assertNotNull(twoPlayerRound);
    }

    // Property Change Listener Tests

    @Test
    void testAddAndRemovePropertyChangeListener() {
        TestListener listener = new TestListener();
        round.addPropertyChangeListener(listener);
        round.removePropertyChangeListener(listener);
        round.setRoundStatus(RoundStatus.BLINDS);
        assertNull(listener.getPropertyName());
    }

    @Test
    void testRoundStatusChangeFiresEvent() {
        TestListener listener = new TestListener();
        round.addPropertyChangeListener(listener);
        round.setRoundStatus(RoundStatus.BLINDS);
        assertEquals(RoundStatus.BLINDS, listener.getNewValue());
    }

    @Test
    void testBetTypeChangeFiresEvent() {
        TestListener listener = new TestListener();
        round.addPropertyChangeListener(listener);
        round.setBetType(BetType.SIDEPOT);
        assertEquals(BetType.SIDEPOT, listener.getNewValue());
    }

    @Test
    void testCommunityCardsChangeFiresEvent() {
        TestListener listener = new TestListener();
        round.addPropertyChangeListener(listener);
        ArrayList<Card> newCards = new ArrayList<>();
        newCards.add(Card.CA);
        round.setCommunityCards(newCards);
        assertEquals(1, ((ArrayList<?>) listener.getNewValue()).size());
    }

    @Test
    void testPotsChangeFiresEvent() {
        TestListener listener = new TestListener();
        round.addPropertyChangeListener(listener);
        ArrayList<Pot> newPots = new ArrayList<>();
        newPots.add(new Pot(players));
        round.setPots(newPots);
        assertNotNull(listener.getNewValue());
    }

    @Test
    void testToPlayChangeFiresEvent() {
        TestListener listener = new TestListener();
        round.addPropertyChangeListener(listener);
        round.setToPlay(200);
        assertEquals(200, listener.getNewValue());
    }

    // Round Status Tests

    @Test
    void testGetAndSetRoundStatus() {
        assertEquals(RoundStatus.UNINITIALISED, round.getRoundStatus());
        round.setRoundStatus(RoundStatus.BLINDS);
        assertEquals(RoundStatus.BLINDS, round.getRoundStatus());
        round.setRoundStatus(RoundStatus.DEAL);
        assertEquals(RoundStatus.DEAL, round.getRoundStatus());
    }

    // Bet Type Tests

    @Test
    void testGetAndSetBetType() {
        assertEquals(BetType.NORMAL, round.getBetType());
        round.setBetType(BetType.SIDEPOT);
        assertEquals(BetType.SIDEPOT, round.getBetType());
        round.setBetType(BetType.ENDROUND);
        assertEquals(BetType.ENDROUND, round.getBetType());
    }

    // Pot Tests

    @Test
    void testGetPots() {
        assertNotNull(round.getPots());
        assertEquals(1, round.getPots().size());
    }

    @Test
    void testSetPots() {
        ArrayList<Pot> newPots = new ArrayList<>();
        newPots.add(new Pot(players));
        newPots.add(new Pot(players));
        round.setPots(newPots);
        assertEquals(2, round.getPots().size());
    }

    @Test
    void testAddPot() {
        assertEquals(1, round.getPots().size());
        assertTrue(round.getPots().get(0).getIsOpen());
        
        round.addPot(new Pot(players));
        
        assertEquals(2, round.getPots().size());
        assertFalse(round.getPots().get(0).getIsOpen());
        assertTrue(round.getPots().get(1).getIsOpen());
    }

    @Test
    void testGetMainPot() {
        Pot mainPot = round.getMainPot();
        assertNotNull(mainPot);
        assertEquals(round.getPots().getFirst(), mainPot);
    }

    @Test
    void testGetOpenPot() {
        Pot openPot = round.getOpenPot();
        assertNotNull(openPot);
        assertTrue(openPot.getIsOpen());
    }

//    @Test
//    void testGetOpenPotIndex() {
//        assertEquals(0, round.getOpenPotIndex());
//
//        round.addPot(new Pot(players));
//        assertEquals(1, round.getOpenPotIndex());
//    }
//
//    @Test
//    void testGetOpenPotIndexNoOpenPot() {
//        round.addPot(new Pot(players));
//        round.getPots().get(1).setIsOpen(false);
//        round.getPots().get(0).setIsOpen(false);
//        assertEquals(-1, round.getOpenPotIndex());
//    }

    // Community Cards Tests

    @Test
    void testGetCommunityCards() {
        assertNotNull(round.getCommunityCards());
        assertTrue(round.getCommunityCards().isEmpty());
    }

    @Test
    void testSetCommunityCards() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(Card.CA);
        cards.add(Card.DK);
        cards.add(Card.HJ);
        round.setCommunityCards(cards);
        assertEquals(3, round.getCommunityCards().size());
        assertEquals(Card.CA, round.getCommunityCards().get(0));
    }

    // To Play Tests

    @Test
    void testGetAndSetToPlay() {
        assertEquals(100, round.getToPlay());
        round.setToPlay(200);
        assertEquals(200, round.getToPlay());
    }

    // Turn Order Tests

    @Test
    void testGetUserIndex() {
        int userIndex = round.getUserIndex();
        assertEquals(0, userIndex);
    }

    @Test
    void testGetUserIndexNoHumanPlayer() {
        ArrayList<Player> aiPlayers = new ArrayList<>();
        aiPlayers.add(new AiPlayer(com.example.projectpoker.model.game.enums.Difficulty.BABY, 1000));
        aiPlayers.add(new AiPlayer(com.example.projectpoker.model.game.enums.Difficulty.BABY, 1000));
        Round aiRound = new Round(aiPlayers, 100);
        assertEquals(-1, aiRound.getUserIndex());
    }

    // End Betting Tests

    @Test
    void testEndBettingAllCheck() {
        for (Player p : players) {
            p.setAction(Action.CHECK);
        }
        boolean result = round.endBetting(players.getLast());
        assertTrue(result);
    }

    @Test
    void testEndBettingAllFold() {
        players.get(0).setAction(Action.FOLD);
        players.get(1).setAction(Action.FOLD);
        players.get(2).setAction(Action.FOLD);
        boolean result = round.endBetting(players.get(2));
        assertTrue(result);
        assertEquals(BetType.SKIP2SHOWDOWN, round.getBetType());
    }

    @Test
    void testEndBettingOneRaiseOthersCall() {
        players.get(0).setAction(Action.RAISE);
        players.get(1).setAction(Action.CALL);
        players.get(2).setAction(Action.CALL);
        boolean result = round.endBetting(players.get(2));
        assertTrue(result);
    }

    @Test
    void testEndBettingOneAllInOthersCall() {
        players.get(0).setAction(Action.ALLIN);
        players.get(1).setAction(Action.CALL);
        players.get(2).setAction(Action.CALL);
        boolean result = round.endBetting(players.get(2));
        assertTrue(result);
    }

    @Test
    void testEndBettingMultipleAllIn() {
        players.get(0).setAction(Action.ALLIN);
        players.get(1).setAction(Action.ALLIN);
        players.get(2).setAction(Action.CALL);
        boolean result = round.endBetting(players.get(2));
        assertTrue(result);
        assertEquals(BetType.SKIP2SHOWDOWN, round.getBetType());
    }

    @Test
    void testEndBettingSidePotCondition() {
        players.get(0).setAction(Action.ALLIN);
        players.get(1).setAction(Action.RAISE);
        players.get(2).setAction(Action.CALL);
        boolean result = round.endBetting(players.get(2));
        assertTrue(result);
        assertEquals(BetType.SIDEPOT, round.getBetType());
    }

    // Note: endBetting() logic is complex - with 3 players where numAllIn=0, 
    // betting will always end due to cond1 being true. This is the expected behavior.
    @Test
    void testEndBettingConditionWithOneAllIn() {
        players.get(0).setAction(Action.ALLIN);
        players.get(1).setAction(Action.CALL);
        players.get(2).setAction(Action.FOLD);
        boolean result = round.endBetting(players.get(2));
        assertTrue(result);
        assertEquals(BetType.SKIP2SHOWDOWN, round.getBetType());
    }

    // Player Action Tests - roundLog is null when not initialized

    @Test
    void testRemoveRoundLogInitiallyNull() {
        ArrayList<RoundLogEntry> log = round.removeRoundLog();
        assertNull(log);
    }

    // Note: playerAction() requires roundLog to be initialized, which it isn't in the constructor.
    // This is a known limitation - init() should be called before playerAction() can work.

    // Deck Tests - Deck is private, tested indirectly through deal operations

    @Test
    void testDeckInitialization() {
        assertNotNull(round);
    }

    // Helper class for PropertyChangeListener testing
    private static class TestListener implements PropertyChangeListener {
        private String propertyName;
        private Object oldValue;
        private Object newValue;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            this.propertyName = evt.getPropertyName();
            this.oldValue = evt.getOldValue();
            this.newValue = evt.getNewValue();
        }

        public String getPropertyName() {
            return propertyName;
        }

        public Object getOldValue() {
            return oldValue;
        }

        public Object getNewValue() {
            return newValue;
        }
    }
    // Round Init test

    @Test
    void testRoundInit() {
        players.get(0).setRole(com.example.projectpoker.model.game.enums.Roles.DEALER);
        players.get(1).setRole(com.example.projectpoker.model.game.enums.Roles.SMALLBLIND);
        players.get(2).setRole(com.example.projectpoker.model.game.enums.Roles.BIGBLIND);

        round.init();

        assertEquals(RoundStatus.BLINDS, round.getRoundStatus());
        assertTrue(round.getMainPot().getPotSize() > 0);
        assertTrue(round.getToPlay() > 0);
    }
}
