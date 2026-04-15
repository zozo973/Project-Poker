package com.example.projectpoker;

import com.example.projectpoker.model.game.TablePosition;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;

import com.example.projectpoker.model.game.Card;

import static com.example.projectpoker.model.game.Card.*;
import static com.example.projectpoker.model.game.TablePosition.*;

public class PokerGameUI extends Application {
    private static final double CARD_WIDTH = 50;

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 320);


        List<Card> Board = List.of(DK, DA, H2);
        List<Card> Hand1 = List.of(SA, HA);
        List<Card> Hand2 = List.of(CT, H7);
        List<Card> Hand3 = List.of(CA, H9);
        List<Card> Hand4 = List.of(H9, HA);
        List<Card> Hand5 = List.of(DQ, SQ);
        List<Card> Hand6 = List.of(C2, H5);
        displayTable(root);
        displayCards(root, Board, BoardPos, true);
        displayCards(root, Hand1, PlayerPos,true);
        displayCards(root, Hand2, TopLeftPos,true);
        displayCards(root, Hand3, TopRightPos,true);
        displayCards(root, Hand4, TopMidPos,true);
        displayCards(root, Hand5, LeftPos,true);
        displayCards(root, Hand6, RightPos,true);
        displayFolded(root, FoldedPos);
        displayDeck(root, DeckPos);
        stage.setTitle("Poker Game");
        stage.setScene(scene);
        stage.show();
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

    private void displayTable(Pane root) {
        Image tableImage = loadImage("/com/example/projectpoker/Images/Poker_Board.png");

        ImageView tableView = new ImageView(tableImage);
        tableView.setFitWidth(800);
        tableView.setPreserveRatio(true);

        root.getChildren().add(tableView);
    }

    private void displayFolded(Pane root, TablePosition position){

        ImageView Deck_Card_Back = new ImageView(loadImage("/com/example/projectpoker/Images/Back1.png"));

        Deck_Card_Back.setFitWidth(50);
        Deck_Card_Back.setPreserveRatio(true);
        Deck_Card_Back.setLayoutX(position.x);
        Deck_Card_Back.setLayoutY(position.y);
        Deck_Card_Back.setRotate(position.rotation);

        root.getChildren().add(Deck_Card_Back);

    }

    private void displayDeck(Pane root, TablePosition position){

        ImageView Deck_Bottom = new ImageView(loadImage("/com/example/projectpoker/Images/Deck_Blank.png"));
        ImageView Deck_Card_Back = new ImageView(loadImage("/com/example/projectpoker/Images/Back1.png"));

        Deck_Bottom.setFitWidth(52);
        Deck_Bottom.setPreserveRatio(true);
        Deck_Bottom.setLayoutX(position.x);
        Deck_Bottom.setLayoutY(position.y);
        Deck_Bottom.setRotate(position.rotation);
        Deck_Card_Back.setFitWidth(50);
        Deck_Card_Back.setPreserveRatio(true);
        Deck_Card_Back.setLayoutX(position.x + position.spacingX);
        Deck_Card_Back.setLayoutY(position.y + position.spacingY);
        Deck_Card_Back.setRotate(position.rotation);

        root.getChildren().add(Deck_Bottom);
        root.getChildren().add(Deck_Card_Back);

    }


    private void displayCards(Pane root, List<Card> cards, TablePosition position, boolean revealed) {

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);

            Image img;
            if (revealed)
            {
                img = loadImage(card.getCardImagePath());
            }
            else
            {
                img = loadImage("/com/example/projectpoker/Images/Back1.png");
            }

            ImageView view = new ImageView(img);

            double width = img.getWidth();
            double height = img.getHeight();

            view.setViewport(new Rectangle2D(0, 0, width, height * position.vScale));

            view.setFitWidth(CARD_WIDTH);
            view.setPreserveRatio(true);

            view.setLayoutX(position.x + i * position.spacingX);
            view.setLayoutY(position.y + i * position.spacingY);
            view.setRotate(position.rotation);

            root.getChildren().add(view);

        }
    }


}
