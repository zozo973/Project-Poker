package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    // Constructor, property change listeners
    // Game status transitions
    // Players management, AI player filtering
    // Blind size configuration
    // Round creation, game ending

    private Game game;
    private Player user;

    @BeforeEach
    void setUp() {
        user = new Player("User", 1000);
        game = new Game(user, 1000, 3, 100, 3, 10, Difficulty.BABY);
    }

    // Constructor Tests

    @Test
    void testConstructorInitializesCorrectly() {
        assertEquals(1, game.getPlayers().size());
        assertEquals(100, game.getBlindSize());
    }

    @Test
    void testConstructorWithDifferentParameters() {
        Player customUser = new Player("Custom", 500);
        Game customGame = new Game(customUser, 500, 5, 200, 5, 20, Difficulty.PROFESSIONAL);
        assertEquals(1, customGame.getPlayers().size());
        assertEquals(200, customGame.getBlindSize());
    }

    // Property Change Listener Tests

    @Test
    void testAddAndRemovePropertyChangeListener() {
        TestListener listener = new TestListener();
        game.addPropertyChangeListener(listener);
        game.removePropertyChangeListener(listener);
        game.setGameStatus(GameStatus.INITIALISED);
        assertNull(listener.getPropertyName());
    }

    @Test
    void testGameStatusChangeFiresEvent() {
        TestListener listener = new TestListener();
        game.addPropertyChangeListener(listener);
        game.setGameStatus(GameStatus.RUNNING);
        assertEquals(GameStatus.RUNNING, listener.getNewValue());
    }

    @Test
    void testBlindSizeChangeFiresEvent() {
        TestListener listener = new TestListener();
        game.addPropertyChangeListener(listener);
        game.setBlindSize(200);
        assertEquals(200, listener.getNewValue());
    }

    @Test
    void testPlayersChangeFiresEvent() {
        TestListener listener = new TestListener();
        game.addPropertyChangeListener(listener);
        ArrayList<Player> newPlayers = new ArrayList<>();
        newPlayers.add(user);
        newPlayers.add(new Player("New2", 500));
        game.setPlayers(newPlayers);
        assertEquals("players", listener.getPropertyName());
    }

    // Game Status Tests

    @Test
    void testGetAndSetGameStatus() {
        assertNull(game.getGameStatus());
        game.setGameStatus(GameStatus.INITIALISED);
        assertEquals(GameStatus.INITIALISED, game.getGameStatus());
        game.setGameStatus(GameStatus.RUNNING);
        assertEquals(GameStatus.RUNNING, game.getGameStatus());
        game.setGameStatus(GameStatus.ENDED);
        assertEquals(GameStatus.ENDED, game.getGameStatus());
    }

    // Players Tests

    @Test
    void testGetPlayers() {
        assertNotNull(game.getPlayers());
        assertEquals(1, game.getPlayers().size());
    }

    @Test
    void testSetPlayers() {
        ArrayList<Player> newPlayers = new ArrayList<>();
        newPlayers.add(new Player("New1", 500));
        newPlayers.add(new Player("New2", 500));
        game.setPlayers(newPlayers);
        assertEquals(2, game.getPlayers().size());
        assertEquals("New1", game.getPlayers().get(0).getName());
    }

    @Test
    void testGetUser() {
        Player retrievedUser = game.getUser();
        assertEquals("User", retrievedUser.getName());
        assertEquals(1000, retrievedUser.getBalance());
    }

    @Test
    void testGetAiPlayersEmptyBeforeInit() {
        ArrayList<AiPlayer> aiPlayers = game.getAiPlayers();
        assertNotNull(aiPlayers);
        assertEquals(0, aiPlayers.size());
    }

    // Test init Method
    @Test
    void testGameInit() {
        game.init();
        assertEquals(GameStatus.INITIALISED,game.getGameStatus());
        assertEquals(3,game.getPlayers().size());
        testGetAiPlayersContainsOnlyAi();
    }

    @Test
    void testGetAiPlayersContainsOnlyAi() {
        ArrayList<AiPlayer> aiPlayers = game.getAiPlayers();
        for (AiPlayer ai : aiPlayers) {
            assertTrue(ai instanceof AiPlayer);
        }
    }

    // Blind Size Tests

    @Test
    void testGetAndSetBlindSize() {
        assertEquals(100, game.getBlindSize());
        game.setBlindSize(200);
        assertEquals(200, game.getBlindSize());
    }

    @Test
    void testIncreaseBlindSize() {
        this.game = new Game(user, 1000, 3, 100, 5, 10, Difficulty.BABY);
        game.setBlindSize(100);
        game.setNumRoundsLeft(5);

        game.tryIncreaseBlind();
        assertEquals(200,game.getBlindSize());
    }

    @Test
    void testBlindSizeChangeFiresCorrectEvent() {
        TestListener listener = new TestListener();
        game.addPropertyChangeListener(listener);
        game.setBlindSize(300);
        assertEquals("blindSize", listener.getPropertyName());
        assertEquals(100, listener.getOldValue());
        assertEquals(300, listener.getNewValue());
    }

    // Round Tests

    @Test
    void testCreateNextRound() {
        game.createNextRound();
        Round round = game.getRound();
        assertNotNull(round);
    }

    @Test
    void testCreateMultipleRounds() {
        game.createNextRound();
        Round firstRound = game.getRound();
        game.createNextRound();
        Round secondRound = game.getRound();
        assertNotEquals(firstRound, secondRound);
    }

    @Test
    void testGetRoundInitiallyNull() {
        assertNull(game.getRound());
    }

    // Start Game Tests

        // Start Game End Condition Tests
    @Test
    void testStartGameNumPlayersEndCondition() {
        // Test when user is the only player left in players
        game.init();
        ArrayList<Player> singlePlayer = new ArrayList<>();
        singlePlayer.add(user);
        game.setPlayers(singlePlayer);
        game.start(true);
        assertEquals(1,game.getPlayers().size());
        assertEquals(GameStatus.ENDED,game.getGameStatus());
    }

    @Test
    void testStartGameUserbalanceEndCondition() {
        // Test Player balance = 0
        game.init();
        ArrayList<Player> players = game.getPlayers();
        int userIndex = game.findUserIndex();
        players.get(userIndex).setBalance(0);
        game.setPlayers(players);
        game.start(true);
        assertEquals(0,game.getUser().getBalance());
        assertEquals(GameStatus.ENDED,game.getGameStatus());
    }

    @Test
    void testStartGameNumRoundEndCondition() {
        // Test when number of rounds left is 0;
        game.init();
        game.setNumRoundsLeft(1);
        game.start(true);
        assertEquals(0,game.getNumRoundsLeft());
        assertEquals(GameStatus.ENDED,game.getGameStatus());
    }

    @Test
    void testStartSetsGameStatusToRunning() {
        game.start();
        assertEquals(GameStatus.RUNNING,game.getGameStatus());
    }

    // End Game Tests

    @Test
    void testEndSetsGameStatusToEnded() {
        game.end();
        assertEquals(GameStatus.ENDED, game.getGameStatus());
    }

    // Test players are initialized with user
    @Test
    void testUserIsFirstPlayer() {
        assertEquals("User", game.getPlayers().getFirst().getName());
    }

    // Test game length configuration
    @Test
    void testGameConfiguration() {
        Player newUser = new Player("Test", 2000);
        Game configuredGame = new Game(newUser, 2000, 6, 50, 2, 15, Difficulty.PROFESSIONAL);
        assertEquals(1, configuredGame.getPlayers().size());
        assertEquals(50, configuredGame.getBlindSize());
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
