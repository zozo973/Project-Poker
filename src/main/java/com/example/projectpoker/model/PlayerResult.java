package com.example.projectpoker.model;
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
}