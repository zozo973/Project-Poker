package com.example.projectpoker;
import com.example.projectpoker.model.*;
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.enums.*;
import com.example.projectpoker.model.statistics.HandStats;
import javafx.application.Application;
import static com.example.projectpoker.model.game.Card.*;

import java.util.*;


public class Launcher {
    public static void main(String[] args) {
        PokerGameUI.launch(PokerGameUI.class, args);


        ArrayList<Card> Board = new ArrayList<>(Arrays.asList(DK, DA, H2, S3, CQ));
        List<Card> P1Hand = List.of(HA, D5);
        List<Card> P2Hand = List.of(CJ, H4);
        Player player1 = new Player("test1");
        Player player2 = new Player("test2");

        for (int i = 0; i < P1Hand.size(); i++) {
            player1.addCardToHand(P1Hand.get(i));
            player2.addCardToHand(P2Hand.get(i));
        }
        ArrayList<Player> testPlayers = new ArrayList<>();
        testPlayers.add(player1);
        testPlayers.add(player2);

        System.out.println(HandEvaluation.whoWins(Board,testPlayers));

    }
}

