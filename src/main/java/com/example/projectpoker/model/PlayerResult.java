package com.example.projectpoker.model;
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.PlayerId;

import java.util.List;

public class PlayerResult {
    private final List<Card> hand;
    private final HandResult result;
    private final PlayerId playerId;

    public PlayerResult(List<Card> hand, HandResult result, PlayerId id) {
        this.hand = hand;
        this.result = result;
        this.playerId = id;
    }

    public List<Card> getHand() { return hand; }
    public HandResult getResult() { return result; }
    public PlayerId getPlayerId() { return playerId; }

    //Finish this later
    public @Override String toString(){
        return "Player wins with " + result.toString();
    }
}