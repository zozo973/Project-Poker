package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.statistics.SkewNormalSampler;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class AiPlayer extends Player {

    private final Difficulty difficulty;

    public AiPlayer(Difficulty difficulty, int playerBalance) {
        super();
        this.difficulty = difficulty;
        this.setBalance(generateAiPlayer(playerBalance));
    }

    private int generateAiPlayer(int playerBalance) {
        int scale = (difficulty.ordinal() - 2);
        SkewNormalSampler skewNormalSampler = new SkewNormalSampler();
        int omega =  SkewNormalSampler.safeRoundToInt(0.1 * playerBalance)*abs(scale) + 1;
        int alpha = SkewNormalSampler.safeRoundToInt(0.05 * scale * playerBalance);
        return skewNormalSampler.sample(playerBalance, omega, alpha);
    }

    @Override
    public ArrayList<Pot> play(ArrayList<Pot> pots, int toPlay) {
        int requiredToCall = PotUtil.getRequiredToCall(pots, this, toPlay);;

        // If player is all-in (balance = 0), they can only check
        if (getBalance() <= 0) {
            setAction(Action.CHECK);
            setActiveBet(0);
            return;
        }

        if (requiredToCall == 0) {
            setAction(Action.CHECK);
            setActiveBet(0);
            return;
        }

        int betAmount = Math.min(requiredToCall, getBalance());
        if (betAmount < requiredToCall) {
            setAction(Action.ALLIN);
        } else {
            setAction(Action.CALL);
        }
        setActiveBet(betAmount);

    }



    @Override
    public void forfeitGame() {
        setAction(Action.FORFEIT);
    }
}
