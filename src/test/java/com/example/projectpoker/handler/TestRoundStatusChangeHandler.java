package com.example.projectpoker.handler;

import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;
import com.example.projectpoker.model.game.enums.Roles;
import com.example.projectpoker.model.game.enums.RoundStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class TestRoundStatusChangeHandler {
    @Test
    void propertyChangeTest() {
        ArrayList<Player> testPlayers = new ArrayList<>();
        testPlayers.add(new Player("test user1",Roles.DEALER));
        testPlayers.add(new Player("test user2",Roles.SMALLBLIND));
        testPlayers.add(new Player("test user3",Roles.BIGBLIND));
        testPlayers.add(new Player("test user4",Roles.PLAYER));

        var round = new Round(

                testPlayers,
                30
        );

        PropertyChangeListener roundStatusChangeHandler = new RoundStatusChangeHandler();
        round.addPropertyChangeListener(roundStatusChangeHandler);

        round.init();

        Assertions.assertEquals(RoundStatus.BLINDS, round.getRoundStatus());
    }
}
