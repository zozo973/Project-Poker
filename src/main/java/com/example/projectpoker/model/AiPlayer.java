package com.example.projectpoker.model;

import com.example.projectpoker.model.statistics.SkewNormalSampler;
import static java.lang.Math.abs;

public class AiPlayer extends Player {

    private Difficulty difficulty;

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
    public int chooseBetSize() {
        // get AI Instance to decide on bet size (as int)
        return 0;
    }
}
