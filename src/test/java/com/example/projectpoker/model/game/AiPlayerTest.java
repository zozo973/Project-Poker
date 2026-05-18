package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Difficulty;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Testing the AI bet inflation bug
class AiPlayerTest {

    @Test
    void fallbackPlayNeverCreatesRaiseAboveBalance() {
        AiPlayer aiPlayer = new AiPlayer(Difficulty.Baby, 1000);
        aiPlayer.setBalance(7170);

        Player opponent = new Player("Opponent", 10000);
        opponent.setAction(Action.RAISE);

        ArrayList<Player> players = new ArrayList<>();
        players.add(aiPlayer);
        players.add(opponent);

        Pot pot = new Pot(players);
        pot.addBet(opponent, 1095);

        ArrayList<Pot> pots = new ArrayList<>();
        pots.add(pot);

        boolean sawRaise = false;
        for (int i = 0; i < 1000; i++) {
            aiPlayer.setAction(Action.UNDECIDED);
            aiPlayer.setActiveBet(null);

            aiPlayer.play(pots);

            Integer activeBet = aiPlayer.getActiveBet();
            if (aiPlayer.getAction() == Action.RAISE) {
                sawRaise = true;
                assertTrue(activeBet != null && activeBet <= aiPlayer.getBalance(),
                        "AI fallback produced a raise above balance: " + activeBet);
            }
        }

        assertTrue(sawRaise, "Test did not observe any raise attempts in fallback path.");
    }
}

