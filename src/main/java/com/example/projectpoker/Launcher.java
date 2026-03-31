package com.example.projectpoker;
import com.example.projectpoker.model.*;
import javafx.application.Application;
import java.util.List;
import java.util.ArrayList;

public class Launcher {
    public static void main(String[] args) {
        //Application.launch(HelloApplication.class, args);

        //CardDeck deck = new CardDeck();
        //deck.shuffle();
        //Player player = new Player(1000);
        //Player ai = new Player(1000);
        //System.out.println(PokerHand.values()[0].getDescription());


        /*for (int i = 0; i < 52; i++) {
            Card test = deck.draw();
            System.out.println(test.getValue());
        }*/
        Card D7 = new Card(Suit.DIAMONDS,Rank.Seven);
        Card D2 = new Card(Suit.DIAMONDS,Rank.Two);
        Card D3 = new Card(Suit.DIAMONDS,Rank.Three);
        Card D4 = new Card(Suit.DIAMONDS,Rank.Four);
        Card D5 = new Card(Suit.DIAMONDS,Rank.Five);
        Card D6 = new Card(Suit.DIAMONDS,Rank.Six);
        List<Card> Cards = new ArrayList<>();

        Cards.add(D2);
        Cards.add(D2);
        Cards.add(D4);
        Cards.add(D4);
        Cards.add(D4);
        Cards.add(D7);
        System.out.println(HandEvaluation.evaluateHand(Cards).toString());

    }
}

