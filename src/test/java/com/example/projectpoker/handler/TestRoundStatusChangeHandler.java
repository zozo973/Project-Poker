package com.example.projectpoker.handler;

import com.example.projectpoker.PokerApplication;
import com.example.projectpoker.controller.RoundController;
import com.example.projectpoker.model.game.AiPlayer;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;
import com.example.projectpoker.model.game.enums.Roles;
import com.example.projectpoker.model.game.enums.RoundStatus;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

public class TestRoundStatusChangeHandler {
    @Test
    void propertyChangeTest() throws IOException {
        ArrayList<Player> testPlayers = new ArrayList<>();
        testPlayers.add(new Player("test user1",Roles.DEALER));
        testPlayers.add(new Player("test user2",Roles.SMALLBLIND));
        testPlayers.add(new Player("test user3",Roles.BIGBLIND));
        testPlayers.add(new Player("test user4",Roles.PLAYER));

        var round = new Round(

                testPlayers,
                30
        );

        FXMLLoader loader = new FXMLLoader(PokerApplication.class.getResource("poker-round-view.fxml"));
        Scene scene = new Scene(loader.load(), 320,240);
        // TODO load css file ...


        RoundController controller = loader.getController();
        controller.setRound(round,testPlayers.getFirst());
        //PropertyChangeListener roundStatusChangeHandler = new RoundStatusChangeHandler(controller);
        //round.addPropertyChangeListener(roundStatusChangeHandler);

        round.init();

        Assertions.assertEquals(RoundStatus.BLINDS, round.getRoundStatus());
    }
}
