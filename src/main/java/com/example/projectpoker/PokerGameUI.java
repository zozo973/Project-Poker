package com.example.projectpoker;

import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.TablePosition;
import com.example.projectpoker.model.game.enums.Roles;
import javafx.animation.FadeTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.projectpoker.model.game.TablePosition.*;

public class PokerGameUI {

    private static final double CARD_WIDTH = 50;
    private static final double CHIP_WIDTH = 40;
    private static final String GREY_NAMEPLATE_STYLE = "-fx-background-color: grey; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8 4 8; -fx-font-size: 11;";
    private static final String YELLOW_NAMEPLATE_STYLE = "-fx-background-color: yellow; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8 4 8; -fx-font-size: 11;";

    private Pane tablePane;
    private final Map<TablePosition, Label> nameplates = new HashMap<>();
    private final Map<TablePosition, FadeTransition> activeNameplateAnimations = new HashMap<>();
    private final List<ImageView> chipViews = new ArrayList<>();

    public void setTablePane(Pane tablePane) {
        this.tablePane = tablePane;
        initialiseTable();
    }

    private Image loadImage(String path) {

        var resource = PokerGameUI.class.getResource(path);

        if (resource == null) {
            throw new RuntimeException("Resource not found: " + path);
        }

        return new Image(resource.toExternalForm());
    }

    public void initialiseTable() {

        if (tablePane == null) return;

        clearChips();
        tablePane.getChildren().clear();
        nameplates.clear();

        displayTable();
        displayDeck(DeckPos);
    }

    public void clearChips() {
        if (tablePane == null) return;
        for (ImageView chip : chipViews) {
            tablePane.getChildren().remove(chip);
        }
        chipViews.clear();
    }

    public void clearNameplates() {
        if (tablePane == null) return;

        for (FadeTransition pulse : activeNameplateAnimations.values()) {
            pulse.stop();
        }
        activeNameplateAnimations.clear();

        for (Label label : nameplates.values()) {
            tablePane.getChildren().remove(label);
        }
        nameplates.clear();
    }

    public void displayNameplate(String playerName, Roles role, TablePosition position, boolean isActiveTurn) {
        if (tablePane == null || position == null) return;

        //Rewrite if already exists
        Label existing = nameplates.remove(position);
        if (existing != null) {
            tablePane.getChildren().remove(existing);
        }

        FadeTransition existingAnimation = activeNameplateAnimations.remove(position);
        if (existingAnimation != null) {
            existingAnimation.stop();
        }

        // Set non-existent players names to "Player"
        String baseName = playerName == null || playerName.isBlank() ? "Player" : playerName;
        Label label = new Label(baseName + roleBadge(role));

        //Change label colour to show active players turn
        label.setStyle(isActiveTurn ? YELLOW_NAMEPLATE_STYLE : GREY_NAMEPLATE_STYLE);

        label.setLayoutX(position.x + position.nameplateOffsetX);
        label.setLayoutY(position.y + position.nameplateOffsetY);
        label.toFront();

        if (isActiveTurn) {
            FadeTransition pulse = new FadeTransition(Duration.millis(450), label);
            pulse.setFromValue(1.0);
            pulse.setToValue(0.6);
            pulse.setCycleCount(FadeTransition.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();
            activeNameplateAnimations.put(position, pulse);
        }

        //Render nameplate
        tablePane.getChildren().add(label);
        nameplates.put(position, label);
    }

    private String roleBadge(Roles role) {
        if (role == null) {
            return "";
        }
        switch (role) {
            case DEALER:
                return " [D]";
            case SMALLBLIND:
                return " [SB]";
            case BIGBLIND:
                return " [BB]";
            default:
                return "";
        }
    }

    private void displayTable() {

        Image tableImage =
                loadImage("/com/example/projectpoker/Images/Poker_Board.png");

        ImageView tableView = new ImageView(tableImage);

        tableView.setFitWidth(800);
        tableView.setPreserveRatio(true);


        tablePane.getChildren().add(tableView);
    }

    private void displayDeck(TablePosition position) {

        ImageView deckBottom = new ImageView(loadImage("/com/example/projectpoker/Images/Deck_Blank.png"));
        ImageView cardBack = new ImageView(loadImage("/com/example/projectpoker/Images/Back1.png"));

        deckBottom.setFitWidth(52);
        deckBottom.setPreserveRatio(true);
        deckBottom.setLayoutX(position.x);
        deckBottom.setLayoutY(position.y);
        deckBottom.setRotate(position.rotation);

        cardBack.setFitWidth(50);
        cardBack.setPreserveRatio(true);
        cardBack.setLayoutX(position.x + position.spacingX);
        cardBack.setLayoutY(position.y + position.spacingY);
        cardBack.setRotate(position.rotation);

        tablePane.getChildren().add(deckBottom);
        tablePane.getChildren().add(cardBack);

    }

    private void displayFolded() {

        ImageView cardBack = new ImageView(loadImage("/com/example/projectpoker/Images/Back1.png"));

        cardBack.setFitWidth(50);
        cardBack.setPreserveRatio(true);
        cardBack.setLayoutX(FoldedPos.x);
        cardBack.setLayoutY(FoldedPos.y);
        cardBack.setRotate(FoldedPos.rotation);
        tablePane.getChildren().add(cardBack);
    }

    public void displayCards(List<Card> cards, TablePosition position, boolean revealed){
        if (tablePane == null) return;

        for (int i = 0; i < cards.size(); i++) {

            Card card = cards.get(i);
            Image img;
            if (revealed) {img = loadImage(card.getCardImagePath());}
            else {img = loadImage("/com/example/projectpoker/Images/Back1.png");}

            ImageView view = new ImageView(img);
            double width = img.getWidth();
            double height = img.getHeight();

            view.setViewport(new Rectangle2D(0,0,width,height * position.vScale));
            view.setFitWidth(CARD_WIDTH);
            view.setPreserveRatio(true);
            view.setLayoutX(position.x + i * position.spacingX);
            view.setLayoutY(position.y + i * position.spacingY);
            view.setRotate(position.rotation);
            view.toFront();

            tablePane.getChildren().add(view);
        }
    }

    public void displayChips(int size, TablePosition position){

        if (size>4 || size <0){throw new RuntimeException("Invalid chip size");}
        // Nothing needs to be rendered
        if (size == 0){return;}

        Image img = loadImage("/com/example/projectpoker/Images/Chips"+ size + ".png");
        ImageView view = new ImageView(img);
        double width = img.getWidth();
        double height = img.getHeight();

        view.setViewport(new Rectangle2D(0,0,width,height));
        view.setFitWidth(position.vScale);
        view.setPreserveRatio(true);
        view.setLayoutX(position.x);
        view.setLayoutY(position.y);

        //Preserve the option to rotate the chips because you never know
        view.setRotate(position.rotation);
        view.toFront();

        tablePane.getChildren().add(view);
        chipViews.add(view);

    }

}