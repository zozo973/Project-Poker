package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    // Constructors, property change listeners, getters/setters
    // Balance, hand, action, turn, role management
    // Betting, pay blind, all-in, forfeit, round reset

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer", 1000);
    }

    // Constructor Tests

    @Test
    void testNoArgsConstructor() {
        Player p = new Player();
        assertEquals("", p.getName());
        assertEquals(1000, p.getBalance());
        assertEquals(Roles.PLAYER, p.getRole());
        assertFalse(p.getIsTurn());
        assertNull(p.getAction());
        assertNotNull(p.getId());
    }

    @Test
    void testConstructorWithName() {
        Player p = new Player("Alice");
        assertEquals("Alice", p.getName());
        assertEquals(1000, p.getBalance());
        assertEquals(Roles.PLAYER, p.getRole());
    }

    @Test
    void testConstructorWithNameAndRole() {
        Player p = new Player("Bob", Roles.BIGBLIND);
        assertEquals("Bob", p.getName());
        assertEquals(Roles.BIGBLIND, p.getRole());
        assertEquals(Action.UNDECIDED, p.getAction());
    }

    @Test
    void testConstructorWithNameAndBalance() {
        Player p = new Player("Charlie", 500);
        assertEquals("Charlie", p.getName());
        assertEquals(500, p.getBalance());
        assertEquals(Roles.PLAYER, p.getRole());
        assertEquals(Action.UNDECIDED, p.getAction());
    }

    @Test
    void testConstructorWithNameBalanceAndId() throws Exception {
        String validId = "ABCDEFGHIJKL";
        Player p = new Player("Dave", 2000, validId);
        assertEquals("Dave", p.getName());
        assertEquals(2000, p.getBalance());
        assertEquals(validId, p.getId().getId());
    }

    @Test
    void testConstructorWithInvalidId() {
        assertThrows(Exception.class, () -> new Player("Eve", 1000, "badid"));
    }

    // Property Change Listener Tests

    @Test
    void testAddAndRemovePropertyChangeListener() {
        TestListener listener = new TestListener();
        player.addPropertyChangeListener(listener);
        player.removePropertyChangeListener(listener);
        player.setBalance(500);
        assertNull(listener.getPropertyName());
    }

    @Test
    void testAddAndRemovePropertyChangeListenerWithName() {
        TestListener listener = new TestListener();
        player.addPropertyChangeListener("balance", listener);
        player.removePropertyChangeListener("balance", listener);
        player.setBalance(500);
        assertNull(listener.getPropertyName());
    }

    @Test
    void testBalanceChangeFiresEvent() {
        TestListener listener = new TestListener();
        player.addPropertyChangeListener("balance", listener);
        player.subtractBalance(100);
        assertEquals("balance", listener.getPropertyName());
        assertEquals(1000, listener.getOldValue());
        assertEquals(900, listener.getNewValue());
    }

    @Test
    void testActionChangeFiresEvent() {
        TestListener listener = new TestListener();
        player.addPropertyChangeListener("action", listener);
        player.setAction(Action.FOLD);
        assertEquals("action", listener.getPropertyName());
        assertEquals(Action.UNDECIDED, listener.getOldValue());
        assertEquals(Action.FOLD, listener.getNewValue());
    }

    @Test
    void testIsTurnChangeFiresEvent() {
        TestListener listener = new TestListener();
        player.addPropertyChangeListener("isTurn", listener);
        player.setIsTurn(true);
        assertEquals("isTurn", listener.getPropertyName());
        assertEquals(false, listener.getOldValue());
        assertEquals(true, listener.getNewValue());
    }

    @Test
    void testRoleChangeFiresEvent() {
        TestListener listener = new TestListener();
        player.addPropertyChangeListener("role", listener);
        player.setRole(Roles.DEALER);
        assertEquals("role", listener.getPropertyName());
        assertEquals(Roles.PLAYER, listener.getOldValue());
        assertEquals(Roles.DEALER, listener.getNewValue());
    }

    // Name Tests

    @Test
    void testGetAndSetName() {
        assertEquals("TestPlayer", player.getName());
        player.setName("NewName");
        assertEquals("NewName", player.getName());
    }

    // ID Tests

    @Test
    void testGetAndSetId() {
        PlayerId newId = new PlayerId();
        player.setId(newId);
        assertEquals(newId, player.getId());
    }

    @Test
    void testMatchId() throws Exception {
        String validId = "ABCDEFGHIJKL";
        Player p1 = new Player("P1", 1000, validId);
        Player p2 = new Player("P2", 1000, validId);
        Player p3 = new Player("P3", 1000, "123456789012");

        assertTrue(p1.matchId(p1.getId()));
        assertTrue(p1.matchId(p2.getId()));
        assertFalse(p1.matchId(p3.getId()));
    }

    // Hand Tests

    @Test
    void testGetPlayerHand() {
        assertNotNull(player.getPlayerHand());
        assertTrue(player.getPlayerHand().getCards().isEmpty());
    }

    @Test
    void testAddCardToHand() {
        player.addCardToHand(Card.CA);
        player.addCardToHand(Card.DK);
        assertEquals(2, player.getPlayerHand().getCards().size());
        assertEquals(Card.CA, player.getPlayerHand().getCards().get(0));
        assertEquals(Card.DK, player.getPlayerHand().getCards().get(1));
    }

    // Balance Tests

    @Test
    void testGetBalance() {
        assertEquals(1000, player.getBalance());
    }

    @Test
    void testSubtractBalance() {
        player.subtractBalance(250);
        assertEquals(750, player.getBalance());
    }

    @Test
    void testSubtractBalanceInsufficientFunds() {
        player.subtractBalance(1500);
        assertEquals(-500, player.getBalance());
    }

    // Round Investment Tests

    @Test
    void testGetAndSetRoundInvestment() {
        assertEquals(0, player.getTotalInvestment());
        player.setRoundInvestment(100);
        assertEquals(100, player.getTotalInvestment());
    }

    // Action Tests

    @Test
    void testGetAndSetAction() {
        assertEquals(Action.UNDECIDED, player.getAction());
        player.setAction(Action.CHECK);
        assertEquals(Action.CHECK, player.getAction());
        player.setAction(Action.RAISE);
        assertEquals(Action.RAISE, player.getAction());
    }

    // Turn Tests

    @Test
    void testGetAndSetIsTurn() {
        assertFalse(player.getIsTurn());
        player.setIsTurn(true);
        assertTrue(player.getIsTurn());
    }

    // Role Tests

    @Test
    void testGetAndSetRole() {
        assertEquals(Roles.PLAYER, player.getRole());
        player.setRole(Roles.SMALLBLIND);
        assertEquals(Roles.SMALLBLIND, player.getRole());
        player.setRole(Roles.BIGBLIND);
        assertEquals(Roles.BIGBLIND, player.getRole());
    }

    // Round Reset Tests

    @Test
    void testRoundReset() {

        player.addCardToHand(Card.CA);
        player.addCardToHand(Card.DK);
        player.setIsTurn(true);
        player.setAction(Action.CHECK);
        player.setRole(Roles.BIGBLIND);
        player.setRoundInvestment(50);

        player.roundReset();

        assertTrue(player.getPlayerHand().getCards().isEmpty());
        assertFalse(player.getIsTurn());
        assertEquals(Action.UNDECIDED, player.getAction());
        assertEquals(Roles.BIGBLIND, player.getRole());
        assertEquals(0, player.getTotalInvestment());
    }

    // Win Tests

    @Test
    void testWin() {
        int initialBalance = player.getBalance();
        int potSize = 500;
        player.win(potSize);
        assertEquals(initialBalance + potSize, player.getBalance());
    }

    // Betting Tests

    @Test
    void testPlaceBetValidBet() {
        int initialBalance = player.getBalance();
        int bet = 100;
        Pot testPot = new Pot();

        int actualBet = player.placeBet(bet, testPot);

        assertEquals(bet, actualBet);
        assertEquals(initialBalance - bet, player.getBalance());
        assertEquals(bet, player.getRoundInvestment().getTotalInvestment());
    }

    @Test
    void testPlaceBetZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> player.placeBet(0, new Pot()));
    }

    @Test
    void testPlaceBetNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> player.placeBet(-50, new Pot()));
    }

    @Test
    void testPlaceBetExceedsBalanceThrows() {
        assertThrows(IllegalArgumentException.class, () -> player.placeBet(2000, new Pot()));
    }

    @Test
    void testPlaceBetUpdatesRoundInvestment() {
        player.placeBet(100, new Pot());
        player.placeBet(50, new Pot());
        assertEquals(150, player.getRoundInvestment().getTotalInvestment());
    }

    // Pay Blind Tests

    @Test
    void testPayBlindAsPlayer() {
        player.setRole(Roles.PLAYER);
        int result = player.payBlind(100, new Pot());
        assertEquals(0, result);
        assertEquals(1000, player.getBalance());
    }

    @Test
    void testPayBlindAsDealer() {
        player.setRole(Roles.DEALER);
        int result = player.payBlind(100, new Pot());
        assertEquals(0, result);
        assertEquals(1000, player.getBalance());
    }

    @Test
    void testPayBlindAsSmallBlind() {
        player.setRole(Roles.SMALLBLIND);
        int result = player.payBlind(100, new Pot());
        assertEquals(50, result);
        assertEquals(950, player.getBalance());
        assertEquals(50, player.getRoundInvestment().getTotalInvestment());
    }

    @Test
    void testPayBlindAsBigBlind() {
        player.setRole(Roles.BIGBLIND);
        int result = player.payBlind(100, new Pot());
        assertEquals(100, result);
        assertEquals(900, player.getBalance());
        assertEquals(100, player.getRoundInvestment().getTotalInvestment());
    }

    @Test
    void testPayBlindAsBigBlindWithShortStackPostsAllInBlind() {
        Player shortStack = new Player("ShortStack", 60);
        shortStack.setRole(Roles.BIGBLIND);

        int result = shortStack.payBlind(100, new Pot());

        assertEquals(60, result);
        assertEquals(0, shortStack.getBalance());
        assertEquals(60, shortStack.getRoundInvestment().getTotalInvestment());
        assertEquals(Action.ALLIN, shortStack.getAction());
    }

    @Test
    void testPayBlindAsBigBlindWithZeroStackPostsNothing() {
        Player zeroStack = new Player("ZeroStack", 0);
        zeroStack.setRole(Roles.BIGBLIND);

        int result = zeroStack.payBlind(100, new Pot());

        assertEquals(0, result);
        assertEquals(0, zeroStack.getBalance());
        assertEquals(0, zeroStack.getRoundInvestment().getTotalInvestment());
        assertEquals(Action.UNDECIDED, zeroStack.getAction());
    }

    // All In Tests

    @Test
    void testAllIn() {
        player.allIn();
        assertEquals(0, player.getBalance());
    }

    @Test
    void testAllInWithZeroBalance() {
        player.subtractBalance(1000);
        int initialBalance = player.getBalance();
        player.allIn();
        assertEquals(0, initialBalance);
    }

    // Forfeit Game Tests

    @Test
    void testForfeitGameWhenFolded() {
        player.setAction(Action.FOLD);
        player.forfeitGame();
        assertEquals(Action.FORFEIT, player.getAction());
    }

    @Test
    void testForfeitGameWhenNotFolded() {
        player.setAction(Action.CHECK);
        player.forfeitGame();
        assertEquals(Action.CHECK, player.getAction());
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
}
