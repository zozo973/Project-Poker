package com.example.projectpoker.model.game;

import com.example.projectpoker.AIActions;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.statistics.SkewNormalSampler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.projectpoker.model.game.PotUtil.getOpenPotIndex;
import static com.example.projectpoker.model.game.PotUtil.getToCall;
import static java.lang.Math.abs;

public class AiPlayer extends Player {

    private final Difficulty difficulty;
    private AIActions.AiPlayerResponse response;


    public AiPlayer(Difficulty difficulty, int playerBalance) {
        super();
        this.difficulty = difficulty;
        this.setBalance(generateAiPlayer(playerBalance));
    }

    public AIActions.AiPlayerResponse getResponse() { return response; }

    public void setResponse(AIActions.AiPlayerResponse response) { this.response = response; }

    private int generateAiPlayer(int playerBalance) {
        int scale = (difficulty.ordinal() - 2);
        SkewNormalSampler skewNormalSampler = new SkewNormalSampler();
        int omega =  SkewNormalSampler.safeRoundToInt(0.1 * playerBalance)*abs(scale) + 1;
        int alpha = SkewNormalSampler.safeRoundToInt(0.05 * scale * playerBalance);
        return skewNormalSampler.sample(playerBalance, omega, alpha);
    }

    @Override
    public void play(ArrayList<Pot> pots) {
        // For Gemini use

        if (this.response != null && this.response.errormsg == null) {
            int toCall = getToCall(pots, this);
            switch (response.action) {
                case CALL:
                    int callAmount = Math.min(toCall, getBalance());
                    if (toCall >= getBalance()) {
                        setAction(Action.ALLIN);
                        setActiveBet(getBalance());
                    } else if (callAmount <= 0) {
                        setAction(Action.CHECK);
                        setActiveBet(0);
                    } else {
                        setAction(Action.CALL);
                        setActiveBet(callAmount);
                    }
                    break;
                case RAISE:
                    int raiseAmount = Math.max(response.amount, getMinBet());
                    if (raiseAmount > getBalance()) {
                        raiseAmount = getBalance();
                    }
                    if (raiseAmount <= toCall) {
                        int callAmt = Math.min(toCall, getBalance());
                        if (callAmt <= 0) {
                            setAction(Action.CHECK);
                            setActiveBet(0);
                        } else {
                            if (callAmt == getBalance()) {
                                setAction(Action.ALLIN);
                                setActiveBet(callAmt);
                            } else {
                                setAction(Action.CALL);
                                setActiveBet(callAmt);
                            }
                        }
                    } else {
                        if (raiseAmount == getBalance()) {
                            setAction(Action.ALLIN);
                            setActiveBet(raiseAmount);
                        } else {
                            setAction(Action.RAISE);
                            setActiveBet(raiseAmount);
                        }
                    }
                    break;

                case ALLIN:
                    setAction(Action.ALLIN);
                    setActiveBet(getBalance());
                    break;
                case FOLD:
                    setAction(Action.FOLD);
                    setActiveBet(0);
                    break;
                case CHECK:
                default:
                    if (toCall == 0) {
                        setAction(Action.CHECK);
                    } else {
                        setAction(Action.FOLD);
                    }
                    setActiveBet(0);
                    break;
            }
        return;
        }
        // For the roles. In case Gemini API not work
        aiFailPlayFallBack(pots);
    }

    private void aiFailPlayFallBack(ArrayList<Pot> pots) {
        int toCall = PotUtil.getToCall(pots, this);

        // If player is all-in (balance = 0), they can only check
        if (getBalance() <= 0) {
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
            int minBet = getMinBet();
            if (toCall != 0) {
                minBet = toCall;
            } else {
                if (minBet == getBalance() && rv < 10) {
                    setActiveBet(getBalance());
                    setAction(Action.ALLIN);
                } else {
                    setActiveBet(0);
                    setAction(Action.FOLD);
                }
                return;
            }
            betAmount = getMinBet() + ((getBalance() - minBet) * (random.nextInt((int) ((Math.floor((double) (getBalance() - minBet) / getMinBet() + 1))))));
        } else {
            if (betAmount == toCall) {
                setAction(Action.CALL);
                setActiveBet(toCall);
                return;
            }
            if (toCall == 0) {
                setAction(Action.CHECK);
                setActiveBet(0);
                return;
            } else setAction(Action.CALL);
        }
        if (betAmount == toCall && getAction().equals(Action.RAISE)) {
            setAction(Action.CALL);
        } else if (betAmount < toCall && getAction().equals(Action.RAISE)) {
            if (rv >= 50) {
                setAction(Action.CALL);
                betAmount = toCall;
            } else {
                setAction(Action.FOLD);
                betAmount = 0;
            }
        }
        setActiveBet(betAmount);
    }

    @Override
    public void forfeitGame() {
        setAction(Action.FORFEIT);
    }
}
