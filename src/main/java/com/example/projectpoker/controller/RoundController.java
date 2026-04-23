package com.example.projectpoker.controller;

import com.example.projectpoker.PokerGameUI;
import com.example.projectpoker.model.game.*;
import com.example.projectpoker.model.game.TablePosition;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.GameStatus;
import com.example.projectpoker.model.game.enums.Roles;
import com.example.projectpoker.model.game.enums.RoundStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class RoundController implements RoundViewUpdater {

    @FXML private Label roundCounterLabel;
    @FXML private Label balanceLabel;
    @FXML private Label potLabel;
    @FXML private Label phaseLabel;
    @FXML private Label toCallLabel;
    @FXML private Slider betSlider;
    @FXML private Button betButton;
    @FXML private Button toCallButton;
    @FXML private Button allInButton;
    @FXML private Button foldButton;
    @FXML private Pane tablePane;
    private PokerGameUI pokerUI;
    private Game game;
    private Round round;
    private Player userPlayer;

    public void setUI(PokerGameUI pokerUI) {
        this.pokerUI = pokerUI;
        pokerUI.setTablePane(tablePane);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void onDealCards() {
        Platform.runLater(() -> {

            if (round.getRoundStatus() == RoundStatus.DEAL) {
                pokerUI.clearCards();
            }
            var round = game.getRound();

            int userIndex = round.getUserIndex();

            var players = game.getPlayers();

            for (int i = 0; i < players.size(); i++) {

                var player = players.get(i);

                boolean revealed =
                        i == userIndex;

                pokerUI.displayCards(
                        player.getPlayerHand().getCards(),
                        TablePosition.PosList.get(i),
                        revealed
                );

            }
        });
    }
    public void setRound(Round round, Player userPlayer) {
        this.round = round;
        this.userPlayer = userPlayer;
        refreshAll();
    }

    @Override
    public void onRoundStarted() {
        Platform.runLater(() -> {
            pokerUI.clearCards();
        });
    }

    private void updateRoundCounterLabel() {

        if (game == null) return;

        roundCounterLabel.setText(
                game.getNumRoundsLeft() + " rounds left."
        );
    }

    @Override
    public void onUserTurnStarted() {
        Platform.runLater(() -> {
            toCallButton.setDisable(false);
            betButton.setDisable(false);
            allInButton.setDisable(false);
            foldButton.setDisable(false);
            updateBetSlider();
        });
    }

    @Override
    public void onAiTurnStarted() {
        Platform.runLater(() -> {
            toCallButton.setDisable(true);
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
    public void onRoundStatusChanged(String phase) {
        Platform.runLater(() ->
                phaseLabel.setText("Phase: " + phase)
        );
    }

    @Override
    public void onCommunityCardsChanged(ArrayList<Card> newCC,ArrayList<Card> oldCC) {

        pokerUI.displayCards(newCC, TablePosition.BoardPos, true);
    }

    @Override
    public void onToPlayChange(int toPlay) { Platform.runLater(() -> toCallLabel.setText(toPlay + "To call" )); }

    @Override
    public void onGameStatusChanged(GameStatus gameStatus) {

        switch (gameStatus) {

            case INITIALISED:

                // Ensure round exists before using it
                if (round == null) {

                    if (game != null) {
                        setRound(game.getRound(), game.getUser());
                    }

                    return;
                }

                if (!round.getRoundStatus()
                        .equals(RoundStatus.UNINITIALISED)) {

                    throw new IllegalArgumentException(
                            "Round must be unInitialised when game is uninitialised"
                    );
                }

                reset();
                break;

            case RUNNING:
                // Animation for blinds
                break;

            case ENDED:
                //var gameLog = game.getGameLog();
                // TODO post-game UI
                break;
        }
    }

    @Override
    public void onRoundCreation(Round round) {
        this.round = round;
    }

    @Override
    public void onPlayerChange(ArrayList<Player> newPlayers, ArrayList<Player> oldPlayers) {
        /*
        for (Player oP : oldPlayers) {
            if (!newPlayers.contains(oP)) {
                // remove player oP from ui as they have left the game;
            }
        }*/

    }

    @Override
    public void onPlayerActionChange(Action action) {
        /*
        if (Action.isBet(action)) {
            // Ui annimation/methods for adding money to pot
        } else if (Action.hasFolded(action)) {
            // In ui gray out player and make there cards disappear.
        } else if (action.equals(Action.CALL)) {
            // Animation of double tap on table.
        }
        */
    }

    @Override
    public void onPlayerRoleUpdate(Roles role) {
        // Each Role should have some differentiable visual and textual feature to differentiate them in the ui.
        switch(role) {
            case Roles.PLAYER:

            case Roles.DEALER:

            case Roles.SMALLBLIND:

            case Roles.BIGBLIND:

        }
    }

    public void reset() {
        toCallLabel.setText(round.getToPlay() + "to play");
        phaseLabel.setText("Status" + RoundStatus.UNINITIALISED);
        potLabel.setText("Pot:" + round.getMainPot());
        balanceLabel.setText("User Balance:" + userPlayer.getBalance());
        updateRoundCounterLabel();
        refreshAll();
    }

    private void refreshAll() {
        if (userPlayer != null) {
            balanceLabel.setText("Balance: " + userPlayer.getBalance());
        }
        if (round != null) {
            potLabel.setText("Pot: " + round.getMainPot());
            phaseLabel.setText("Phase: " + round.getRoundStatus());
        }
        updateRoundCounterLabel();
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
