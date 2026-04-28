package com.example.projectpoker.controller;

import com.example.projectpoker.AiCoaching;
import com.example.projectpoker.model.game.enums.AiAdviceMode;

import com.example.projectpoker.PokerGameUI;
import com.example.projectpoker.model.game.*;
import com.example.projectpoker.model.game.TablePosition;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.GameStatus;
import com.example.projectpoker.model.game.enums.RoundStatus;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


public class RoundController {

    public BorderPane mainBorderPane;
    @FXML private Label roundCounterLabel;
    @FXML private Label balanceLabel;
    @FXML private Label potLabel;
    @FXML private Label phaseLabel;
    @FXML private Label toCallLabel;
    @FXML private Slider betSlider;
    @FXML private Button betButton;
    @FXML private Button toCallButton;
    @FXML private Button allInButton;
    @FXML private Button startRoundButton;
    @FXML private Button foldButton;
    @FXML private Button startGameButton;
    @FXML private Pane tablePane;

    // For Ai Coaching
    @FXML private ToggleButton btnSafe;
    @FXML private ToggleButton btnNormal;
    @FXML private ToggleButton btnRisky;
    @FXML private Button btnGenerate;
    @FXML private Label aiActionLabel;
    @FXML private Label aiReasonLabel;
    @FXML private Label aiStatusLabel;

    private final AiCoaching aiCoaching = new AiCoaching();
    private AiAdviceMode currentAiMode = AiAdviceMode.NORMAL;
    private PokerGameUI pokerUI;
    private Game game;
    private Round round;
    private Player userPlayer;
    private Player activeTurnPlayer;
    private volatile boolean roundTransitionInProgress;
    private final ArrayList<Player> observedPlayers = new ArrayList<>();
    private final PropertyChangeListener gameListener = this::handleGameEvent;
    private final PropertyChangeListener roundListener = this::handleRoundEvent;
    private final PropertyChangeListener playerListener = this::handlePlayerEvent;

    public void setUI(PokerGameUI pokerUI) {
        this.pokerUI = pokerUI;
        pokerUI.setTablePane(tablePane);
        disableActionButtons();
    }

    public void setGame(Game game) {
        if (this.game != null) {
            this.game.removePropertyChangeListener(gameListener);
        }

        this.game = game;
        if (game == null) {
            return;
        }

        game.addPropertyChangeListener(gameListener);
        registerPlayerListeners(game.getPlayers());
    }

    private void onDealCards() {
        runOnFxThread(() -> renderHoleCardsNow(false, true));
    }

    private void revealOpponentCardsAtShowdown() {
        runOnFxThread(() -> renderHoleCardsNow(true, false));
    }

    private void renderHoleCardsNow(boolean revealOpponents, boolean clearBeforeRender) {
        if (round == null || game == null || pokerUI == null || userPlayer == null) {
            return;
        }

        if (clearBeforeRender) {
            pokerUI.initialiseTable();
        }

        ArrayList<Player> players = game.getPlayers();

        // Render user hand first
        pokerUI.displayCards(userPlayer.getPlayerHand().getCards(), TablePosition.PlayerPos, true);

        int seatIndex = 1;
        for (Player player : players) {
            if (player == userPlayer) {continue;}
            if (seatIndex >= TablePosition.PosList.size()) {break;}

            if (player.getAction() != Action.FOLD) {
                // At showdown this draws face-up cards over previously drawn card backs.
                pokerUI.displayCards(player.getPlayerHand().getCards(), TablePosition.PosList.get(seatIndex), revealOpponents);
            }
            seatIndex++;
        }

        renderNameplatesNow();
        renderChipStacksNow();
    }

    public void setRound(Round round, Player userPlayer) {
        if (this.round != null) {
            this.round.removePropertyChangeListener(roundListener);
        }

        this.round = round;
        this.userPlayer = userPlayer;
        if (round != null) {
            round.addPropertyChangeListener(roundListener);
        }
        runOnFxThread(this::refreshAll);
    }

    private void updateRoundCounterLabel() {

        if (game == null) return;

        roundCounterLabel.setText(
                game.getNumRoundsLeft() + " rounds left."
        );
    }

    private void onUserTurnStarted() {
        Platform.runLater(() -> {
            toCallButton.setDisable(false);
            betButton.setDisable(false);
            allInButton.setDisable(false);
            foldButton.setDisable(false);
            updateBetSlider();
        });
    }

    private void onBalanceChanged(Player player, int oldBalance, int newBalance) {
        Platform.runLater(() -> {
            renderChipStacksNow();
            if (player == userPlayer) {
                balanceLabel.setText("Balance: " + newBalance);
                updateCallDisplay();
                updateBetSlider();

                if (newBalance > oldBalance) {
                    int winnings = newBalance - oldBalance;
                    System.out.println("You just won the round congratulations you won " + winnings + " from that round");
                }
            }
        });
    }

    private void onPotChanged(int newPot) {
        Platform.runLater(() -> {
            potLabel.setText("Pot: " + newPot);
                pokerUI.displayPotChipStack(newPot);
            updateCallDisplay();
        });
    }

    private void onCommunityCardsChanged(ArrayList<Card> newCC) {
        if (pokerUI == null || newCC == null) {
            return;
        }
        Platform.runLater(() -> pokerUI.displayCards(newCC, TablePosition.BoardPos, true));
    }

    private void onGameStatusChanged(GameStatus gameStatus) {

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

            case ENDED:
                //var gameLog = game.getGameLog();
                // TODO post-game UI
                break;

            default:
                break;
        }
    }

    private void onRoundCreation(Round round) {
        Player currentUser = userPlayer;
        if (currentUser == null && game != null) {
            currentUser = game.getUser();
        }
        setRound(round, currentUser);
    }

    private void onPlayerChange(ArrayList<Player> newPlayers) {
        registerPlayerListeners(newPlayers);
        runOnFxThread(() -> {
            renderNameplatesNow();
            renderChipStacksNow();
        });
    }

    private void redrawTableState() {
        if (round == null || game == null || pokerUI == null) {
            return;
        }

        runOnFxThread(() -> {
            boolean revealOpponents = round.getRoundStatus() == RoundStatus.SHOWDOWN;
            pokerUI.initialiseTable();

            if (round.getCommunityCards() != null && !round.getCommunityCards().isEmpty()) {
                pokerUI.displayCards(round.getCommunityCards(), TablePosition.BoardPos, true);
            }
            renderHoleCardsNow(revealOpponents, false);
        });
    }

    private void renderNameplatesNow() {
        if (game == null || pokerUI == null || userPlayer == null) {
            return;
        }

        pokerUI.clearNameplates();
        pokerUI.displayNameplate(userPlayer.getName(), userPlayer.getRole(), TablePosition.PlayerPos, userPlayer == activeTurnPlayer);

        int seatIndex = 1;
        for (Player player : game.getPlayers()) {
            if (player == userPlayer) {
                continue;
            }
            if (seatIndex >= TablePosition.PosList.size()) {
                break;
            }

            pokerUI.displayNameplate(
                    player.getName(),
                    player.getRole(),
                    TablePosition.PosList.get(seatIndex),
                    player == activeTurnPlayer
            );
            seatIndex++;
        }
    }

    private void renderChipStacksNow() {
        if (game == null || pokerUI == null || userPlayer == null) {
            return;
        }

        pokerUI.clearPlayerChipStacks();
        pokerUI.displayPlayerChipStack(userPlayer.getBalance(), TablePosition.PlayerPos);

        int seatIndex = 1;
        for (Player player : game.getPlayers()) {
            if (player == userPlayer) {
                continue;
            }
            if (seatIndex >= TablePosition.PosList.size()) {
                break;
            }

            pokerUI.displayPlayerChipStack(player.getBalance(), TablePosition.PosList.get(seatIndex));
            seatIndex++;
        }
    }

    private void reset() {
        runOnFxThread(() -> {
            updateCallDisplay();
            phaseLabel.setText("Status" + RoundStatus.UNINITIALISED);
            potLabel.setText("Pot: " + getTotalPotSize());
            balanceLabel.setText("User Balance:" + userPlayer.getBalance());
            updateRoundCounterLabel();
            refreshAll();
        });
    }

    private void refreshAll() {
        if (userPlayer != null) {
            balanceLabel.setText("Balance: " + userPlayer.getBalance());
        }
        if (round != null) {
            potLabel.setText("Pot: " + getTotalPotSize());
            pokerUI.displayPotChipStack(getTotalPotSize());

            phaseLabel.setText("Phase: " + round.getRoundStatus());
        }
        renderNameplatesNow();
        renderChipStacksNow();
        updateCallDisplay();
        updateRoundCounterLabel();
        updateBetSlider();
    }

    private void updateCallDisplay() {
        int amountToCall = getAmountToCall();
        toCallLabel.setText(amountToCall + " to call");
        toCallButton.setText(amountToCall > 0 ? "Call" : "Check");
    }

    private int getTotalPotSize() {
        if (round == null || round.getPots() == null) {
            return 0;
        }

        int total = 0;
        for (Pot pot : round.getPots()) {
            total += pot.getPotSize();
        }
        return total;
    }

    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    private void updateBetSlider() {
        if (userPlayer == null || round == null) return;

        int balance = userPlayer.getBalance();
        int amountToCall = getAmountToCall();
        int blind = game != null ? game.getBlindSize() : 0;
        int minBetUnit = Math.max(blind, 1);

        toCallButton.setText(amountToCall > 0 ? "Call" : "Check");

        if (balance <= 0) {
            betSlider.setDisable(true);
            betSlider.setMin(0);
            betSlider.setMax(0);
            betSlider.setValue(0);
            return;
        }

        int minBet = Math.min(minBetUnit, balance);
        int maxBet = Math.max(minBet, (balance / minBetUnit) * minBetUnit);

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
        betSlider.setMajorTickUnit(minBetUnit);
        betSlider.setBlockIncrement(minBetUnit);

        double current = betSlider.getValue();
        if (current < minBet) current = minBet;
        if (current > maxBet) current = maxBet;
        betSlider.setValue(current);
    }

    @FXML
    private void onStartGameButton() {
        if (roundTransitionInProgress) {
            return;
        }
        roundTransitionInProgress = true;
        Thread gameThread = new Thread(game::start, "poker-game-loop");
        gameThread.setDaemon(true);
        gameThread.start();
        this.startGameButton.setVisible(false);
        setStartRoundButtonEnabled(false);

    }

    @FXML
    private void onStartRoundButton() {
        if (roundTransitionInProgress || game == null || round == null || round.getRoundStatus() != RoundStatus.END) {
            return;
        }

        roundTransitionInProgress = true;
        startRoundButton.setDisable(true);
        Thread nextRoundThread = new Thread(game::onRoundEnded, "poker-next-round");
        nextRoundThread.setDaemon(true);
        nextRoundThread.start();
    }

    @FXML
    private void handleCall() {
        int amountToCall = getAmountToCall();
        if (amountToCall > 0) {
            submitPlayerAction(Action.CALL, amountToCall);
        } else {
            submitPlayerAction(Action.CHECK, 0);
        }
        disableActionButtons();
    }
    @FXML
    private void handleRaise() {
        int raiseAmount = (int) betSlider.getValue();
        submitPlayerAction(Action.RAISE, raiseAmount);
        disableActionButtons();
    }
    @FXML
    private void handleFold() {
        submitPlayerAction(Action.FOLD,0);
        disableActionButtons();
    }
    @FXML
    private void handleAllIn() {
        userPlayer.setActiveBet(userPlayer.getBalance());
        submitPlayerAction(Action.ALLIN, userPlayer.getBalance());
        disableActionButtons();
    }

    private void disableActionButtons() {

        toCallButton.setDisable(true);
        betButton.setDisable(true);
        foldButton.setDisable(true);
        allInButton.setDisable(true);
    }

    private void submitPlayerAction(Action action, int amount) {

        if (userPlayer == null) return;

        userPlayer.setAction(action);
        userPlayer.setActiveBet(amount);

        System.out.println("Action submitted: " + action + " amount=" + amount
        );


        // Release the betting loop
        userPlayer.setIsTurn(false);
    }

    private int getAmountToCall() {
        if (round == null || userPlayer == null) {
            return 0;
        }
        return Math.max(0, round.getToPlay() - userPlayer.getTotalInvestment());
    }

    private void handleGameEvent(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "gameStatus":
            case "state":
                onGameStatusChanged((GameStatus) evt.getNewValue());
                break;
            case "blindSize":
                Platform.runLater(this::updateBetSlider);
                break;
            case "round":
                onRoundCreation((Round) evt.getNewValue());
                break;
            case "players":
                @SuppressWarnings("unchecked")
                ArrayList<Player> newPlayers = (ArrayList<Player>) evt.getNewValue();
                onPlayerChange(newPlayers);
                break;
            default:
                break;
        }
    }

    private void handleRoundEvent(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "pots":
                if (round != null) {
                    onPotChanged(getTotalPotSize());
                }
                break;
            case "state":
                executeRoundStateUpdate((RoundStatus) evt.getNewValue());
                break;
            case "communityCards":
                @SuppressWarnings("unchecked")
                ArrayList<Card> newCC = (ArrayList<Card>) evt.getNewValue();
                onCommunityCardsChanged(newCC);
                break;
            case "toPlay":
                Platform.runLater(this::updateCallDisplay);
                break;
            default:
                break;
        }
    }

    private void handlePlayerEvent(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "isTurn":
                handleTurnChange(evt);
                break;
            case "balance":
                handleBalanceChange(evt);
                break;
            case "action":
                if (Action.hasFolded((Action) evt.getNewValue())) {
                    redrawTableState();
                }
                break;
            case "role":
                runOnFxThread(() -> {
                    renderNameplatesNow();
                    renderChipStacksNow();
                });
                break;
            default:
                break;
        }
    }

    private void handleTurnChange(PropertyChangeEvent evt) {
        boolean isTurn = (boolean) evt.getNewValue();
        if (!(evt.getSource() instanceof Player turnPlayer)) {
            return;
        }

        if (isTurn) {
            activeTurnPlayer = turnPlayer;
            runOnFxThread(() -> {
                renderNameplatesNow();
                renderChipStacksNow();
            });

            if (turnPlayer instanceof AiPlayer) {
                Platform.runLater(this::disableActionButtons);
            } else {
                onUserTurnStarted();
            }
            return;
        }

        if (turnPlayer == activeTurnPlayer) {
            activeTurnPlayer = null;
            runOnFxThread(() -> {
                renderNameplatesNow();
                renderChipStacksNow();
            });
        }
    }

    private void handleBalanceChange(PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof Player player)) {
            return;
        }

        onBalanceChanged(player, (int) evt.getOldValue(), (int) evt.getNewValue());
    }

    private void executeRoundStateUpdate(RoundStatus status) {
        Platform.runLater(() ->phaseLabel.setText("Phase: " + status.name()));
        if (status != RoundStatus.END) {
            roundTransitionInProgress = false;
        }
        setStartRoundButtonEnabled(status == RoundStatus.END);
        switch (status) {
            case UNINITIALISED:
                break;
            case DEAL:
                onDealCards();
                break;
            case FLOP:
            case TURN:
            case RIVER:
                if (round != null) {
                    onCommunityCardsChanged(round.getCommunityCards());
                }
                break;
            case SHOWDOWN:
                if (round != null) {
                    onCommunityCardsChanged(round.getCommunityCards());
                    revealOpponentCardsAtShowdown();
                }
                break;
            default:
                break;
        }
    }

    private void setStartRoundButtonEnabled(boolean enabled) {
        if (startRoundButton == null) {
            return;
        }
        Platform.runLater(() -> startRoundButton.setDisable(!enabled));
    }

    private void registerPlayerListeners(List<Player> players) {
        for (Player player : observedPlayers) {
            player.removePropertyChangeListener(playerListener);
        }
        observedPlayers.clear();

        if (players == null) {
            return;
        }

        for (Player player : players) {
            player.addPropertyChangeListener(playerListener);
            observedPlayers.add(player);
        }
    }


    @FXML
    private void handleAiMode() {
        if (btnSafe.isSelected())       currentAiMode = AiAdviceMode.SAFE;
        else if (btnRisky.isSelected()) currentAiMode = AiAdviceMode.RISKY;
        else                            currentAiMode = AiAdviceMode.NORMAL;
    }

    @FXML
    private void handleAiGenerate() {
        btnGenerate.setDisable(true);
        aiStatusLabel.setText("Asking AI...");
        aiActionLabel.setText("Action: -");
        aiReasonLabel.setText("Reason: -");

        Card[] hand  = userPlayer.getPlayerHand().getCards().toArray(new Card[0]);
        Card[] board = round.getCommunityCards().toArray(new Card[0]);
        RoundStatus status = round.getRoundStatus();


        Task<AiCoaching.AiAdvice> task = new Task<>() {
            @Override protected AiCoaching.AiAdvice call() {
                return aiCoaching.getAdvice(hand, board, status, currentAiMode);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            AiCoaching.AiAdvice advice = task.getValue();
            if (advice.errormsg != null) {
                aiActionLabel.setText("Action: -");
                aiReasonLabel.setText("Reason: -");
                aiStatusLabel.setText("Error: " + advice.errormsg);
            } else {
                aiActionLabel.setText("Action: " + advice.action + " (" + advice.confidence + "%)");
                aiReasonLabel.setText("Reason: " + advice.reason);
                aiStatusLabel.setText("Done.");
            }
            btnGenerate.setDisable(false);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            aiStatusLabel.setText("Error: Connection failed.");
            btnGenerate.setDisable(false);
        }));
        new Thread(task, "AiCoach-Thread").start();
    }
}

