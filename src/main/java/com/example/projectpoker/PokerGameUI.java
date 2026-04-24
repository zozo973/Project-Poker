package com.example.projectpoker;

import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.TablePosition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.List;

import static com.example.projectpoker.model.game.TablePosition.*;

public class PokerGameUI {

    private static final double CARD_WIDTH = 50;

    private Pane tablePane;

    public void setTablePane(Pane tablePane) {
        this.tablePane = tablePane;
        initialiseTable();
    }

    private Image loadImage(String path) {

        var resource = PokerGameUI.class.getResource(path);

        if (resource == null) {
            throw new RuntimeException(
                    "Resource not found: " + path
            );
        }

        return new Image(resource.toExternalForm());
    }

    private void initialiseTable() {

        if (tablePane == null) return;

        tablePane.getChildren().clear();

        displayTable();
        displayDeck(DeckPos);
        displayFolded(FoldedPos);
    }

    public void clearCards() {
        initialiseTable();
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

        ImageView deckBottom =
                new ImageView(loadImage(
                        "/com/example/projectpoker/Images/Deck_Blank.png"
                ));

        ImageView cardBack =
                new ImageView(loadImage(
                        "/com/example/projectpoker/Images/Back1.png"
                ));

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

    private void displayFolded(TablePosition position) {

        ImageView cardBack =
                new ImageView(loadImage(
                        "/com/example/projectpoker/Images/Back1.png"
                ));

        cardBack.setFitWidth(50);
        cardBack.setPreserveRatio(true);
        cardBack.setLayoutX(position.x);
        cardBack.setLayoutY(position.y);
        cardBack.setRotate(position.rotation);

        tablePane.getChildren().add(cardBack);
    }

    public void displayCards(List<Card> cards, TablePosition position, boolean revealed){
        if (tablePane == null) return;

        for (int i = 0; i < cards.size(); i++) {

            Card card = cards.get(i);

            Image img;

            if (revealed) {
                img = loadImage(card.getCardImagePath());
            }
            else {
                img = loadImage(
                        "/com/example/projectpoker/Images/Back1.png"
                );
            }

            ImageView view = new ImageView(img);

            double width = img.getWidth();
            double height = img.getHeight();

            view.setViewport(
                    new Rectangle2D(
                            0,
                            0,
                            width,
                            height * position.vScale
                    )
            );

            view.setFitWidth(CARD_WIDTH);
            view.setPreserveRatio(true);

            view.setLayoutX(
                    position.x + i * position.spacingX
            );

            view.setLayoutY(
                    position.y + i * position.spacingY
            );

            view.setRotate(position.rotation);
            view.toFront();
            tablePane.getChildren().add(view);


        }
    }
}