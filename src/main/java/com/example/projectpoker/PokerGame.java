package com.example.projectpoker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;

import com.example.projectpoker.model.game.Card;

import static com.example.projectpoker.model.game.Card.*;

public class PokerGame extends Application {


    @Override
    public void start(Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600);




        List<Card> Board = List.of(DK, DA, H2, S3, CQ);
        List<Card> Hand = List.of(SA, HA);
        // Display the hand

        displayTable(root);
        displayBoard(root, Board);
        displayPlayerHand(root, Hand);
        displayFolded(root);
        displayDeck(root);

        stage.setTitle("Poker Game");
        stage.setScene(scene);
        stage.show();
    }

    private void displayPlayerHand(Pane root, List<Card> cards){

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);

            Image img = new Image(PokerGame.class.getResource(card.getCardImagePath(card)).toExternalForm());
            ImageView view = new ImageView(img);

            view.setFitWidth(50);
            view.setPreserveRatio(true);

            view.setLayoutX(300 + i * 15);
            view.setLayoutY(295);

            root.getChildren().add(view);
        }

    }

    private void displayFolded(Pane root){

        ImageView Deck_Card_Back = new ImageView(new Image(PokerGame.class.getResource("/com/example/projectpoker/Images/Back1.png").toExternalForm()));

        Deck_Card_Back.setFitWidth(50);
        Deck_Card_Back.setPreserveRatio(true);
        Deck_Card_Back.setLayoutX(152);
        Deck_Card_Back.setLayoutY(150);
        Deck_Card_Back.setRotate(90);

        root.getChildren().add(Deck_Card_Back);

    }

    private void displayDeck(Pane root){

        ImageView Deck_Bottom = new ImageView(new Image(PokerGame.class.getResource("/com/example/projectpoker/Images/Deck_Blank.png").toExternalForm()));
        ImageView Deck_Card_Back = new ImageView(new Image(PokerGame.class.getResource("/com/example/projectpoker/Images/Back1.png").toExternalForm()));

        Deck_Bottom.setFitWidth(52);
        Deck_Bottom.setPreserveRatio(true);
        Deck_Bottom.setLayoutX(150);
        Deck_Bottom.setLayoutY(85);
        Deck_Bottom.setRotate(90);
        Deck_Card_Back.setFitWidth(50);
        Deck_Card_Back.setPreserveRatio(true);
        Deck_Card_Back.setLayoutX(150);
        Deck_Card_Back.setLayoutY(87);
        Deck_Card_Back.setRotate(90);

        root.getChildren().add(Deck_Bottom);
        root.getChildren().add(Deck_Card_Back);

    }

    private void displayBoard(Pane root, List<Card> cards) {
        //root.getChildren().clear();

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);

            Image img = new Image(PokerGame.class.getResource(card.getCardImagePath(card)).toExternalForm());
            ImageView view = new ImageView(img);

            view.setFitWidth(50);
            view.setPreserveRatio(true);

            view.setLayoutX(250 + i * 54);
            view.setLayoutY(123);

            root.getChildren().add(view);
        }
    }

    private void displayTable(Pane root) {
        Image tableImage = new Image(getClass().getResource("/com/example/projectpoker/Images/Poker_Board.png").toExternalForm());

        ImageView tableView = new ImageView(tableImage);
        tableView.setFitWidth(800);
        tableView.setPreserveRatio(true);

        root.getChildren().add(tableView);
    }

}
