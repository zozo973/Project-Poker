package com.example.projectpoker.controller;

import com.example.projectpoker.AiCoaching;
import com.example.projectpoker.model.GamePreferences;
import com.example.projectpoker.model.game.enums.AiAdviceMode;

import com.example.projectpoker.PokerGameUI;
import com.example.projectpoker.model.game.*;
import com.example.projectpoker.model.game.TablePosition;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.GameStatus;
import com.example.projectpoker.model.game.enums.RoundStatus;
import com.example.projectpoker.service.GamePreferencesService;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


import java.io.IOException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class RoundController {
    private static final int BET_STEP = 5;
    private static final int MENU_WIDTH = 420;
    private static final int MENU_HEIGHT = 550;
    private static final String DARK_MODE_CLASS = "game-dark";
    private static final String DARK_THEME_STYLESHEET = "/com/example/projectpoker/poker-round-dark.css";


    public BorderPane mainBorderPane;
    @FXML private Label roundCounterLabel;
    @FXML private Label balanceLabel;
    @FXML private Label potLabel;
    @FXML private Label phaseLabel;
    @FXML private Label toCallLabel;
    @FXML private Spinner<Integer> betSpinner;
    @FXML private Button betButton;
    @FXML private Button toCallButton;
    @FXML private Button allInButton;
    @FXML private Button startRoundButton;
    @FXML private Button foldButton;
    @FXML private Button startGameButton;
    @FXML private Pane tablePane;
    @FXML private CheckMenuItem fullscreenToggle;
    @FXML private CheckMenuItem darkModeToggle;

    // For Ai Coaching
    @FXML private ToggleButton btnSafe;
    @FXML private ToggleButton btnNormal;
    @FXML private ToggleButton btnRisky;
    @FXML private Button btnGenerate;
    @FXML private Label aiActionLabel;
    @FXML private Label aiReasonLabel;
    @FXML private Label aiStatusLabel;
    @FXML private TextArea gameLogTextArea;

    private final AiCoaching aiCoaching = new AiCoaching();
    private final GamePreferencesService preferencesService = new GamePreferencesService();
    private AiAdviceMode currentAiMode = AiAdviceMode.NORMAL;
    private PokerGameUI pokerUI;
    private Game game;
    private Round round;
    private Player userPlayer;
    private Player activeTurnPlayer;
    private volatile boolean roundTransitionInProgress;
    private boolean preferredDarkMode;
    private final ArrayList<Player> observedPlayers = new ArrayList<>();
    private final PropertyChangeListener gameListener = this::handleGameEvent;
    private final PropertyChangeListener roundListener = this::handleRoundEvent;
    private final PropertyChangeListener playerListener = this::handlePlayerEvent;

    public void setUI(PokerGameUI pokerUI) {
        this.pokerUI = pokerUI;
        pokerUI.setTablePane(tablePane);
        disableActionButtons();

        if (mainBorderPane != null) {
            mainBorderPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    if (darkModeToggle != null) {
                        darkModeToggle.setSelected(preferredDarkMode);
                    }
                    applyDarkMode(preferredDarkMode);
                }
            });
        }
    }

    public void setPreferredDarkMode(boolean enabled) {
        preferredDarkMode = enabled;
        if (darkModeToggle != null) {
            darkModeToggle.setSelected(enabled);
        }
        applyDarkMode(enabled);
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
            appendGameLog("--- Round " + round.getRoundNumber() + " ---");
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
            // TODO User should not be able to fold if they can check
            foldButton.setDisable(false);
            updateBetInput();
        });
    }

    private void onBalanceChanged(Player player, int oldBalance, int newBalance) {
        Platform.runLater(() -> {
            renderChipStacksNow();
            if (player == userPlayer) {
                balanceLabel.setText("Balance: " + newBalance);
                updateCallDisplay();
                updateBetInput();

                if (newBalance > oldBalance) {
                    int winnings = newBalance - oldBalance;
                    appendGameLog("You won " + winnings + " chips this round.");
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
                            "Round must be uninitialised when game is uninitialised"
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

        runOnFxThread(this::redrawTableStateNow);
    }

    private void redrawTableStateNow() {
        boolean revealOpponents = round.getRoundStatus() == RoundStatus.SHOWDOWN;
        pokerUI.initialiseTable();

        if (round.getCommunityCards() != null && !round.getCommunityCards().isEmpty()) {
            pokerUI.displayCards(round.getCommunityCards(), TablePosition.BoardPos, true);
        }
        renderHoleCardsNow(revealOpponents, false);
    }

    private void renderCardsBeforeBetting() {
        if (round == null || game == null || pokerUI == null) {
            return;
        }

        if (Platform.isFxApplicationThread()) {
            redrawTableStateNow();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                redrawTableStateNow();
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
        updateBetInput();
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

    private void updateBetInput() {
        if (userPlayer == null || round == null) return;

        int balance = userPlayer.getBalance();
        int amountToCall = getAmountToCall();
        int maxRaise = Math.max(0, (balance / BET_STEP) * BET_STEP);

        toCallButton.setText(amountToCall > 0 ? "Call" : "Check");

        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxRaise, 0, BET_STEP);
        betSpinner.setValueFactory(valueFactory);
        betSpinner.setEditable(true);

        if (maxRaise <= 0) {
            betSpinner.setDisable(true);
            return;
        }
        betSpinner.setDisable(false);
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
    private void quitToMainMenu() {
        if (game == null) {
            navigateToMainMenu();
            return;
        }

        Thread closeThread = new Thread(() -> {
            try {
                game.closeSession();
                Platform.runLater(this::navigateToMainMenu);
            } catch (Exception ex) {
                Platform.runLater(() -> aiStatusLabel.setText("Error: Could not close game session."));
            }
        }, "poker-close-session");
        closeThread.setDaemon(true);
        closeThread.start();
    }

    private void navigateToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectpoker/MainMenu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            stage.setScene(new Scene(root, MENU_WIDTH, MENU_HEIGHT));
            stage.setTitle("PokerPro+");
            stage.setFullScreen(false);
            stage.show();
        } catch (IOException ex) {
            aiStatusLabel.setText("Error: Could not return to main menu.");
        }
    }

    @FXML
    private void toggleFullscreen() {
        if (mainBorderPane == null || mainBorderPane.getScene() == null) {
            return;
        }
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
        stage.setFullScreen(fullscreenToggle != null && fullscreenToggle.isSelected());
    }

    @FXML
    private void toggleDarkMode() {
        boolean enabled = darkModeToggle != null && darkModeToggle.isSelected();
        preferredDarkMode = enabled;
        applyDarkMode(enabled);
        saveDarkModePreference(enabled);
    }

    private void applyDarkMode(boolean enabled) {
        if (mainBorderPane == null || mainBorderPane.getScene() == null) {
            return;
        }

        Scene scene = mainBorderPane.getScene();
        String stylesheet = getClass().getResource(DARK_THEME_STYLESHEET).toExternalForm();
        if (!scene.getStylesheets().contains(stylesheet)) {
            scene.getStylesheets().add(stylesheet);
        }

        if (enabled) {
            if (!scene.getRoot().getStyleClass().contains(DARK_MODE_CLASS)) {
                scene.getRoot().getStyleClass().add(DARK_MODE_CLASS);
            }
        } else {
            scene.getRoot().getStyleClass().remove(DARK_MODE_CLASS);
        }
    }

    private void saveDarkModePreference(boolean enabled) {
        GamePreferences current = preferencesService.loadForCurrentUser();
        GamePreferences updated = new GamePreferences(
                current.getOpponentCount(),
                current.getDifficulty(),
                current.getCardBackKey(),
                current.getBoardKey(),
                enabled
        );
        preferencesService.saveForCurrentUser(updated);
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
        Integer selected = betSpinner.getValue();
        int raiseAmount = selected == null ? 0 : selected;
        if (raiseAmount <= 0) {
            return;
        }
        int maxRaise = Math.max(0, (userPlayer.getBalance() / BET_STEP) * BET_STEP);
        raiseAmount = Math.min(raiseAmount, maxRaise);
        raiseAmount = (raiseAmount / BET_STEP) * BET_STEP;
        if (raiseAmount <= 0) {
            return;
        }
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
        if (betSpinner != null) {
            betSpinner.setDisable(true);
        }
        foldButton.setDisable(true);
        allInButton.setDisable(true);
    }

    private void submitPlayerAction(Action action, int amount) {

        if (userPlayer == null) return;

        userPlayer.setAction(action);
        userPlayer.setActiveBet(amount);

        if (action == Action.CALL && amount > 0) {
            appendGameLog("You call " + amount + ".");
        } else if (action == Action.RAISE && amount > 0) {
            appendGameLog("You raise to " + amount + ".");
        } else if (action == Action.ALLIN) {
            appendGameLog("You go all-in for " + amount + ".");
        } else if (action == Action.CHECK) {
            appendGameLog("You check.");
        } else if (action == Action.FOLD) {
            appendGameLog("You fold.");
        }


        // Release the betting loop
        userPlayer.setIsTurn(false);
    }

    private int getAmountToCall() {
        if (round == null || userPlayer == null) {
            return 0;
        }
        return Math.max(0, round.getTotalToPlay() - userPlayer.getTotalInvestment());
    }

    private void handleGameEvent(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "gameStatus":
            case "state":
                onGameStatusChanged((GameStatus) evt.getNewValue());
                break;
            case "blindSize":
                Platform.runLater(this::updateBetInput);
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
                executeRoundStateUpdate((RoundStatus) evt.getNewValue(), (RoundStatus) evt.getOldValue());
                break;
            case "communityCards":
                @SuppressWarnings("unchecked")
                ArrayList<Card> newCC = (ArrayList<Card>) evt.getNewValue();
                onCommunityCardsChanged(newCC);
                break;
            case "toPlay":
                Platform.runLater(this::updateCallDisplay);
                break;
            case "logEntry":
                appendGameLog(String.valueOf(evt.getNewValue()));
                break;
            default:
                break;
        }
    }

    private void appendGameLog(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        runOnFxThread(() -> {
            if (gameLogTextArea == null) {
                return;
            }
            if (!gameLogTextArea.getText().isEmpty()) {
                gameLogTextArea.appendText("\n---------\n");
            }
            gameLogTextArea.appendText(message);
            int endPosition = gameLogTextArea.getLength();
            gameLogTextArea.positionCaret(endPosition);
            Platform.runLater(() -> gameLogTextArea.positionCaret(gameLogTextArea.getLength()));
        });
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

    private void executeRoundStateUpdate(RoundStatus status, RoundStatus previousStatus) {
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
            case BETTING1, BETTING2, BETTING3, BETTING4:
                renderCardsBeforeBetting();
                break;
            case FLOP, TURN, RIVER:
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
            case END:
                // Round can advance SHOWDOWN -> END before the UI processes SHOWDOWN; reveal here as a fallback.
                if (previousStatus == RoundStatus.SHOWDOWN && round != null) {
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

            if (userPlayer == null || userPlayer.getPlayerHand() == null || userPlayer.getPlayerHand().getCards() == null) {
                aiStatusLabel.setText("AI unavailable: player hand not ready yet.");
                btnGenerate.setDisable(false);
                return;
            }

            if (round == null || round.getCommunityCards() == null) {
                aiStatusLabel.setText("Advice unavailable: Please start the game 1st.");
                btnGenerate.setDisable(false);
                return;
            }

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

