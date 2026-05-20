package com.example.projectpoker.controller;

import com.example.projectpoker.model.GamePreferences;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.service.GamePreferencesService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.util.List;

public class OptionsMenuController {

    private final GamePreferencesService preferencesService = new GamePreferencesService();
    private final List<GamePreferences.AssetOption> cardBackOptions = GamePreferences.CARD_BACK_OPTIONS;
    private final List<GamePreferences.AssetOption> boardOptions = GamePreferences.BOARD_OPTIONS;
    private int cardBackIndex;
    private int boardIndex;
    private FadeTransition fadeTransition;

    @FXML private Label messageLabel;
    @FXML private Spinner<Integer> opponentsSpinner;
    @FXML private ComboBox<Difficulty> difficultyComboBox;
    @FXML private Label difficultyDescriptionLabel;
//    @FXML private Label cardBackNameLabel;
    @FXML private ImageView cardBackPreview;
//    @FXML private Label boardNameLabel;
    @FXML private ImageView boardPreview;
//    @FXML private CheckBox darkModeCheckBox;

    @FXML
    private void initialize() {
        opponentsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        GamePreferences.MIN_OPPONENTS,
                        GamePreferences.MAX_OPPONENTS,
                        GamePreferences.DEFAULT_OPPONENTS
                )
        );

        difficultyComboBox.setItems(FXCollections.observableArrayList(Difficulty.values()));

        GamePreferences loaded = preferencesService.loadForCurrentUser();
        opponentsSpinner.getValueFactory().setValue(loaded.getOpponentCount());
        difficultyComboBox.setValue(loaded.getDifficulty());
//        darkModeCheckBox.setSelected(loaded.isDarkModeEnabled());

        cardBackIndex = indexForKey(cardBackOptions, loaded.getCardBackKey());
        boardIndex = indexForKey(boardOptions, loaded.getBoardKey());

        refreshDifficultyDescription();
        refreshCardBackPreview();
        refreshBoardPreview();
    }

    @FXML
    private void onDifficultyChanged() {
        refreshDifficultyDescription();
    }

    @FXML
    private void previousCardBack() {
        cardBackIndex = (cardBackIndex - 1 + cardBackOptions.size()) % cardBackOptions.size();
        refreshCardBackPreview();
    }

    @FXML
    private void nextCardBack() {
        cardBackIndex = (cardBackIndex + 1) % cardBackOptions.size();
        refreshCardBackPreview();
    }

    @FXML
    private void previousBoard() {
        boardIndex = (boardIndex - 1 + boardOptions.size()) % boardOptions.size();
        refreshBoardPreview();
    }

    @FXML
    private void nextBoard() {
        boardIndex = (boardIndex + 1) % boardOptions.size();
        refreshBoardPreview();
    }

    @FXML
    private void savePreferences() {
        Difficulty selectedDifficulty = difficultyComboBox.getValue();
        if (selectedDifficulty == null) {
            selectedDifficulty = GamePreferences.DEFAULT_DIFFICULTY;
        }

//        boolean darkModeEnabled = darkModeCheckBox != null && darkModeCheckBox.isSelected();

        GamePreferences preferences = new GamePreferences(
                opponentsSpinner.getValue(),
                selectedDifficulty,
                cardBackOptions.get(cardBackIndex).key(),
                boardOptions.get(boardIndex).key()
//                darkModeEnabled
        );

        preferencesService.saveForCurrentUser(preferences);
        messageLabel.setOpacity(1.0);
        messageLabel.setText("Preferences Saved!");
        fadeTransition = new FadeTransition(Duration.seconds(1), messageLabel);
        fadeTransition.setDelay(Duration.seconds(5));
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(e -> messageLabel.setText(""));
        fadeTransition.play();
    }

    @FXML
    private void goToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/projectpoker/MainMenu.fxml"));
            Parent root = loader.load();
            messageLabel.getScene().setRoot(root);
        } catch (IOException e) {
            messageLabel.setText("Couldn't Load Main Menu");
        }
    }

    private void refreshDifficultyDescription() {
        Difficulty difficulty = difficultyComboBox.getValue();
        if (difficulty == null) {
            difficultyDescriptionLabel.setText("");
            return;
        }
        difficultyDescriptionLabel.setText(difficulty.getDescription());
    }

    private void refreshCardBackPreview() {
        GamePreferences.AssetOption selected = cardBackOptions.get(cardBackIndex);
//        cardBackNameLabel.setText(selected.displayName());
        cardBackPreview.setImage(loadImage(selected.resourcePath()));
    }

    private void refreshBoardPreview() {
        GamePreferences.AssetOption selected = boardOptions.get(boardIndex);
//        boardNameLabel.setText(selected.displayName());
        boardPreview.setImage(loadImage(selected.resourcePath()));
    }

    private Image loadImage(String path) {
        var resource = getClass().getResource(path);
        if (resource == null) {
            throw new IllegalStateException("Missing Preview Image: " + path);
        }
        return new Image(resource.toExternalForm());
    }

    private int indexForKey(List<GamePreferences.AssetOption> options, String key) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).key().equals(key)) {
                return i;
            }
        }
        return 0;
    }
}
