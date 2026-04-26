package com.example.projectpoker;

import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.TablePosition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.projectpoker.model.game.TablePosition.*;

public class PokerGameUI {

    private static final double CARD_WIDTH = 50;

    private Pane tablePane;
    private final Map<TablePosition, Label> nameplates = new HashMap<>();

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

        tablePane.getChildren().clear();
        nameplates.clear();

        displayTable();
        displayDeck(DeckPos);
    }

    public void clearNameplates() {
        if (tablePane == null) return;
        for (Label label : nameplates.values()) {
            tablePane.getChildren().remove(label);
        }
        nameplates.clear();
    }

    public void displayNameplate(String playerName, TablePosition position, boolean isActiveTurn) {
        if (tablePane == null || position == null) return;

        //Rewrite if already exists
        Label existing = nameplates.remove(position);
        if (existing != null) {
            tablePane.getChildren().remove(existing);
        }
        // Set non-existent players names to "Player"
        Label label = new Label(playerName == null || playerName.isBlank() ? "Player" : playerName);

        //Change label colour to show active players turn
        label.setStyle(isActiveTurn
                ? "-fx-background-color: yellow; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8 4 8; -fx-font-size: 11;"
                : "-fx-background-color: grey; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8 4 8; -fx-font-size: 11;");

        // Get coordinates
        double[] xy = getNameplateCoordinates(position);
        label.setLayoutX(xy[0]);
        label.setLayoutY(xy[1]);
        label.toFront();

        //Render nameplate
        tablePane.getChildren().add(label);
        nameplates.put(position, label);
    }

    private double[] getNameplateCoordinates(TablePosition position) {
        if (position == PlayerPos) {
            return new double[]{position.x - 55, position.y - 10};
        }
        if (position == LeftPos) {
            return new double[]{position.x + 20, position.y + 60};
        }
        if (position == RightPos) {
            return new double[]{position.x - 15, position.y + 40};
        }
        // Top seats
        return new double[]{position.x - 70, position.y + 5};
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
}