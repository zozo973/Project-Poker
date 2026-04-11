package com.example.projectpoker.handler;

import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;

public class TestGameStatusChangeHandler {
    @Test
    void propertyChangeTest() {
        var game = new Game(
                new Player("Test User", 5000),
                5000,
                4,
                20,
                5,
                40,
                Difficulty.PROFESSIONAL
        );

        PropertyChangeListener gameStatusChangeHandler = new GameStatusChangeHandler();
        game.addPropertyChangeListener(gameStatusChangeHandler);

        game.init();

        Assertions.assertEquals(GameStatus.INITIALISED, game.getGameStatus());
    }
}
