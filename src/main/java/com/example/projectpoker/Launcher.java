package com.example.projectpoker;

import javafx.application.Application;
import com.example.projectpoker.GameLogic.*;

public class Launcher {
/*    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }

 */

    public static void main(String[] args) {
        Deck deck = new Deck();
        Board board = new Board();

        Player player = new Player("You");
        Player ai = new Player("Bot");

        player.getHand().addCard(deck.drawCard());
        player.getHand().addCard(deck.drawCard());

        ai.getHand().addCard(deck.drawCard());
        ai.getHand().addCard(deck.drawCard());

        System.out.println(player.getName() + ": " + player.getHand());
        System.out.println(ai.getName() + ": " + ai.getHand());


// FLOP (3 cards)
        board.addCard(deck.drawCard());
        board.addCard(deck.drawCard());
        board.addCard(deck.drawCard());

        System.out.println("Flop: " + board);

// TURN (1 card)
        board.addCard(deck.drawCard());
        System.out.println("Turn: " + board);

// RIVER (1 card)
        board.addCard(deck.drawCard());
        System.out.println("River: " + board);
    }


}
