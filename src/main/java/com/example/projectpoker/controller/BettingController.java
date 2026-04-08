package com.example.projectpoker.controller;

import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;

public class BettingController implements RoundViewUpdater {


    @FXML
    private Slider betSlider;
    @FXML
    private Label betLabel;
    @FXML
    private Label balanceLabel;

    private Round round;
    private Player user;

    private IntegerProperty blindSize;
    private final PropertyChangeListener balanceListener = this::handlePlayerPropertyChange;


    private void handlePlayerPropertyChange(PropertyChangeEvent evt) {
        if ("balance".equals(evt.getPropertyName())) {
            Platform.runLater(this::refreshUI);
        }
    }

    @FXML
    private void handleBet() {
        if (round == null) return;

        int betAmount = (int) betSlider.getValue();
        user.placeBet(betAmount);

        // Usually refreshUI() is not strictly necessary here because
        // the balance change event will trigger it, but it is fine to omit.
    }

    @FXML
    private void handleAllIn() {
        if (round == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm All In");
        alert.setHeaderText("Go all in?");
        alert.setContentText("Are you sure you want to bet your entire remaining balance?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            user.allIn();
        }
    }

    public void cleanup() {
        if (user != null) {
            user.removePropertyChangeListener("balance", balanceListener);
        }
    }

    // bet amount slider method
    // @FXML
    // protected void onBetSlider() {
    //    Code for
    // }

    // bet button click method
    // @FXML
    // protected void onBetButtonClick() {
    //
    // }
    private void configureSlider() {
        betSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (round == null) return;

            int blind = round.getToPlay();
           int snapped = snapToBlind(newVal.intValue(), blind);

            if (snapped != newVal.intValue()) {
                betSlider.setValue(snapped);
                return;
            }

            betLabel.setText("Bet: " + snapped);
        });
    }

    private int snapToBlind(int value, int blind) {
        if (blind <= 0) return 0;
        return Math.max(blind, (int) Math.round((double) value / blind) * blind);
    }


    private void refreshUI() {
        if (round == null ) return;

        Player player = user;
        int balance = player.getBalance();
        int blind = round.getToPlay();

        balanceLabel.setText("Balance: " + balance);

        if (balance <= 0 || blind <= 0) {
            betSlider.setDisable(true);
            betSlider.setMin(0);
            betSlider.setMax(0);
            betSlider.setValue(0);
            betLabel.setText("Bet: 0");
            return;
        }

        int minBet = Math.min(blind, balance);
        int maxBet = (balance / blind) * blind;

        if (maxBet < minBet) {
            // Not enough chips for a normal blind-sized bet
            betSlider.setDisable(true);
            betSlider.setMin(0);
            betSlider.setMax(0);
            betSlider.setValue(0);
            betLabel.setText("Bet: 0");
            return;
        }

        betSlider.setDisable(false);
        betSlider.setMin(minBet);
        betSlider.setMax(maxBet);
        betSlider.setMajorTickUnit(blind);
        betSlider.setMinorTickCount(0);
        betSlider.setBlockIncrement(blind);

        int currentValue = snapToBlind((int) betSlider.getValue(), blind);
        if (currentValue < minBet) currentValue = minBet;
        if (currentValue > maxBet) currentValue = maxBet;

        betSlider.setValue(currentValue);
        betLabel.setText("Bet: " + currentValue);
    }

    @Override
    public void onUserTurnStarted() {

    }

    @Override
    public void onAiTurnStarted() {

    }

    @Override
    public void onBalanceChanged(Player player, int oldBalance, int newBalance) {

    }

    @Override
    public void onBlindSizeChanged(int newBlindSize) {

    }

    @Override
    public void onPotChanged(int newPot) {

    }

    @Override
    public void onRoundPhaseChanged(String phase) {

    }
}
