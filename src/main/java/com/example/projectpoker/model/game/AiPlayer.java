package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.statistics.SkewNormalSampler;

import java.util.ArrayList;
import java.util.Random;

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
    public void play(ArrayList<Pot> pots) {
        int toCall = PotUtil.getToCall(pots, this);

        // If player is all-in (balance = 0), they can only check
        if (getBalance() <= 0) {
            setAction(Action.CHECK);
            setActiveBet(0);
            return;
        }

        if (toCall == 0) {
            setAction(Action.CHECK);
            setActiveBet(0);
            return;
        }
        Random random = new Random();
        int rv = random.nextInt((100)+1);

        int betAmount = Math.min(toCall, getBalance());
        if (betAmount < toCall) {
            setAction(Action.ALLIN);
        } else if (rv < 2) { // 2% chance to go All-in
            setAction(Action.ALLIN);
            betAmount = getBalance();
        } else if (rv < 20) { // 20% chance to raise
            setAction(Action.RAISE);
            betAmount = getMinBet() * (random.nextInt((int) ((Math.floor((double) getBalance() /getMinBet()) - 1) + 1)) + 1);
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
