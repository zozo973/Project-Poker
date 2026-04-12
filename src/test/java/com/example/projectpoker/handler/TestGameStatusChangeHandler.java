package com.example.projectpoker.handler;

import com.example.projectpoker.PokerApplication;
import com.example.projectpoker.controller.RoundController;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.io.IOException;

public class TestGameStatusChangeHandler {
    @Test
    void propertyChangeTest() throws IOException {
        var game = new Game(
                new Player("Test User", 5000),
                5000,
                4,
                20,
                5,
                40,
                Difficulty.PROFESSIONAL
        );

        FXMLLoader loader = new FXMLLoader(PokerApplication.class.getResource("poker-round-view.fxml"));
        Scene scene = new Scene(loader.load(), 320,240);
        // TODO load css file ...


        RoundController controller = loader.getController();
        // controller.setRound(round,testPlayers.getFirst());

        PropertyChangeListener gameStatusChangeHandler = new GameStatusChangeHandler(controller);
        game.addPropertyChangeListener(gameStatusChangeHandler);

        game.init();

        Assertions.assertEquals(GameStatus.INITIALISED, game.getGameStatus());
    }
}
