package com.example.projectpoker.controller;

import com.example.projectpoker.PokerApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.util.List;

public class TutorialController {

    @FXML private ImageView tutorialImageView;
    @FXML private Label imageCaptionLabel;
    @FXML private Label pageIndicatorLabel;
    @FXML private Label messageLabel;
    @FXML private Button previousButton;
    @FXML private Button nextButton;

    private int currentIndex = 0;

    // Illustrations/Screenshots of how to use UI, how to play poker, etc. for Tutorial
    private final String[] tutorialImages = {
            // please add the actual screenshots/illustrations
            "/images/back1.png",
            "images/back2.png",
            "/images/tutorial/randomExample.png"
    };
    // Tutorial Captions
    private final String[] tutorialCaptions = {
            // please add the actual screenshots/illustrations
            "Blah Blah Blah.",
            "Blah Blah Blah2",
            "Blah Blah Blah 3."
    }; // NOTE MAKE SURE TO KEEP BOTH ARRAYS SAME SIZE SO THEY MATCH 1:1

    @FXML
    public void initialize() {
        showSlide(currentIndex);
    }

    private void showSlide(int index) {
        // Update captions
        if (index < tutorialCaptions.length) {
            imageCaptionLabel.setText(tutorialCaptions[index]);
        } else {
            imageCaptionLabel.setText("");
        }

        // Update page indicator
        pageIndicatorLabel.setText((index + 1) + " / " + tutorialImages.length);

        // Load and display image
        var resource = getClass().getResource(tutorialImages[index]);
        if (resource != null) {
            tutorialImageView.setImage(new Image(resource.toExternalForm()));
            messageLabel.setText("");
        } else {
            // Placeholder if image not yet added by teammate
            tutorialImageView.setImage(null);
            messageLabel.setText("Slide " + (index + 1) + " image coming soon.");
        }

        // Update navigation button states
        previousButton.setDisable(index == 0);
        nextButton.setDisable(index == tutorialImages.length - 1);
    }

    @FXML
    private void previousTutorialScreenshot() {
        if (currentIndex > 0) {
            currentIndex--;
            showSlide(currentIndex);
        }
    }

    @FXML
    private void nextTutorialScreenshot() {
        if (currentIndex < tutorialImages.length - 1) {
            currentIndex++;
            showSlide(currentIndex);
        }
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
    @FXML
    private void goToGame() {
        try {
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            PokerApplication app = new PokerApplication();
            app.createPokerGame(stage);
        } catch (IOException e) {
            messageLabel.setText("Could not start game: " + e.getMessage());
        }
    }
}
