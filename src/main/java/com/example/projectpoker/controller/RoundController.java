package com.example.projectpoker.controller;

import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RoundController implements RoundViewUpdater {

    @FXML private Label balanceLabel;
    @FXML private Label potLabel;
    @FXML private Label phaseLabel;
    @FXML private Slider betSlider;
    @FXML private Button betButton;
    @FXML private Button allInButton;
    @FXML private Button foldButton;

    private Round round;
    private Player userPlayer;

    public void setRound(Round round, Player userPlayer) {
        this.round = round;
        this.userPlayer = userPlayer;
        refreshAll();
    }

    @Override
    public void onUserTurnStarted() {
        Platform.runLater(() -> {
            betButton.setDisable(false);
            allInButton.setDisable(false);
            foldButton.setDisable(false);
            updateBetSlider();
        });
    }

    @Override
    public void onAiTurnStarted() {
        Platform.runLater(() -> {
            betButton.setDisable(true);
            allInButton.setDisable(true);
            foldButton.setDisable(true);
        });
    }

    @Override
    public void onBalanceChanged(Player player, int oldBalance, int newBalance) {
        Platform.runLater(() -> {
            if (player == userPlayer) {
                balanceLabel.setText("Balance: " + newBalance);
                updateBetSlider();

                if (newBalance > oldBalance) {
                    int winnings = newBalance - oldBalance;
                    System.out.println("You just won the round congratulations you won " + winnings + " from that round");
                }
            }
        });
    }

    @Override
    public void onBlindSizeChanged(int newBlindSize) {
        Platform.runLater(this::updateBetSlider);
    }

    @Override
    public void onPotChanged(int newPot) {
        Platform.runLater(() -> potLabel.setText("Pot: " + newPot));
    }

    @Override
    public void onRoundPhaseChanged(String phase) {
        Platform.runLater(() -> phaseLabel.setText("Phase: " + phase));
    }

    private void refreshAll() {
        if (userPlayer != null) {
            balanceLabel.setText("Balance: " + userPlayer.getBalance());
        }
        if (round != null) {
            potLabel.setText("Pot: " + round.getMainPot());
            phaseLabel.setText("Phase: " + round.getRoundStatus());
        }
        updateBetSlider();
    }
    private void updateBetSlider() {
        if (userPlayer == null || round == null) return;

        int balance = userPlayer.getBalance();
        int blind = round.getToPlay();

        if (balance <= 0 || blind <= 0) {
            betSlider.setDisable(true);
            betSlider.setMin(0);
            betSlider.setMax(0);
            betSlider.setValue(0);
            return;
        }

        int minBet = Math.min(blind, balance);
        int maxBet = (balance / blind) * blind;

        if (maxBet < minBet) {
            betSlider.setDisable(true);
            betSlider.setMin(0);
            betSlider.setMax(0);
            betSlider.setValue(0);
            return;
        }

        betSlider.setDisable(false);
        betSlider.setMin(minBet);
        betSlider.setMax(maxBet);
        betSlider.setMajorTickUnit(blind);
        betSlider.setBlockIncrement(blind);

        double current = betSlider.getValue();
        if (current < minBet) current = minBet;
        if (current > maxBet) current = maxBet;
        betSlider.setValue(current);
    }
}
