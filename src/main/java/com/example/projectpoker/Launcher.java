package com.example.projectpoker;
import com.example.projectpoker.model.*;
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.enums.*;
import com.example.projectpoker.model.statistics.HandStats;
import javafx.application.Application;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Launcher {
    public static void main(String[] args) {
        //Application.launch(HelloApplication.class, args);



        Card C2 = new Card(Suit.Clubs, Rank.Two);
        Card C3 = new Card(Suit.Clubs,Rank.Three);
        Card C4 = new Card(Suit.Clubs,Rank.Four);
        Card C5 = new Card(Suit.Clubs,Rank.Five);
        Card C6 = new Card(Suit.Clubs,Rank.Six);
        Card C7 = new Card(Suit.Clubs, Rank.Seven);
        Card C8 = new Card(Suit.Clubs,Rank.Eight);
        Card C9 = new Card(Suit.Clubs,Rank.Nine);
        Card C0 = new Card(Suit.Clubs,Rank.Ten);
        Card CJ = new Card(Suit.Clubs,Rank.Jack);
        Card CQ = new Card(Suit.Clubs,Rank.Queen);
        Card CK = new Card(Suit.Clubs, Rank.King);
        Card CA = new Card(Suit.Clubs, Rank.Ace);

        Card D2 = new Card(Suit.Diamonds,Rank.Two);
        Card D3 = new Card(Suit.Diamonds,Rank.Three);
        Card D4 = new Card(Suit.Diamonds,Rank.Four);
        Card D5 = new Card(Suit.Diamonds,Rank.Five);
        Card D6 = new Card(Suit.Diamonds,Rank.Six);
        Card D7 = new Card(Suit.Diamonds, Rank.Seven);
        Card D8 = new Card(Suit.Diamonds,Rank.Eight);
        Card D9 = new Card(Suit.Diamonds,Rank.Nine);
        Card D0 = new Card(Suit.Diamonds,Rank.Ten);
        Card DJ = new Card(Suit.Diamonds,Rank.Jack);
        Card DQ = new Card(Suit.Diamonds,Rank.Queen);
        Card DK = new Card(Suit.Diamonds, Rank.King);
        Card DA = new Card(Suit.Diamonds, Rank.Ace);

        Card H2 = new Card(Suit.Hearts,Rank.Two);
        Card H3 = new Card(Suit.Hearts,Rank.Three);
        Card H4 = new Card(Suit.Hearts,Rank.Four);
        Card H5 = new Card(Suit.Hearts,Rank.Five);
        Card H6 = new Card(Suit.Hearts,Rank.Six);
        Card H7 = new Card(Suit.Hearts, Rank.Seven);
        Card H8 = new Card(Suit.Hearts,Rank.Eight);
        Card H9 = new Card(Suit.Hearts,Rank.Nine);
        Card H0 = new Card(Suit.Hearts,Rank.Ten);
        Card HJ = new Card(Suit.Hearts,Rank.Jack);
        Card HQ = new Card(Suit.Hearts,Rank.Queen);
        Card HK = new Card(Suit.Hearts, Rank.King);
        Card HA = new Card(Suit.Hearts, Rank.Ace);

        Card S2 = new Card(Suit.Spades,Rank.Two);
        Card S3 = new Card(Suit.Spades,Rank.Three);
        Card S4 = new Card(Suit.Spades,Rank.Four);
        Card S5 = new Card(Suit.Spades,Rank.Five);
        Card S6 = new Card(Suit.Spades,Rank.Six);
        Card S7 = new Card(Suit.Spades, Rank.Seven);
        Card S8 = new Card(Suit.Spades,Rank.Eight);
        Card S9 = new Card(Suit.Spades,Rank.Nine);
        Card S0 = new Card(Suit.Spades,Rank.Ten);
        Card SJ = new Card(Suit.Spades,Rank.Jack);
        Card SQ = new Card(Suit.Spades,Rank.Queen);
        Card SK = new Card(Suit.Spades, Rank.King);
        Card SA = new Card(Suit.Spades, Rank.Ace);


        ArrayList<Card> Board = (ArrayList<Card>) List.of(DK, DA, H2, S3, CQ);
        List<Card> P1Hand = List.of(HA, D5);
        List<Card> P2Hand = List.of(CJ, H4);
        Player player1 = new Player("test1");
        Player player2 = new Player("test2");

        for (int i = 0; i < P1Hand.size(); i++) {
            player1.addToPlayerHand(P1Hand.get(i));
            player2.addToPlayerHand(P2Hand.get(i));
        }
        ArrayList<Player> testPlayers = new ArrayList<>();
        testPlayers.add(player1);
        testPlayers.add(player2);

        System.out.println(HandEvaluation.whoWins(Board,testPlayers));

    }
}

