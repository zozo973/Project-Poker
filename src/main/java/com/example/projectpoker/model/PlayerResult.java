package com.example.projectpoker.model;
import com.example.projectpoker.model.game.Card;

import java.util.List;

public class PlayerResult {
    private List<Card> hand;
    private HandResult result;

    public PlayerResult(List<Card> hand, HandResult result) {
        this.hand = hand;
        this.result = result;
    }

    public List<Card> getHand() { return hand; }
    public HandResult getResult() { return result; }

    //Finish this later
    public @Override String toString(){
        return "Player wins with " + result.toString();
    }
}