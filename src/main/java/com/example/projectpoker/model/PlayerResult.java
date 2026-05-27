package com.example.projectpoker.model;
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.PlayerId;

import java.util.List;

public class PlayerResult {
    private final List<Card> hand;
    private final HandResult result;
    private final PlayerId playerId;

    /**
     * Creates a result snapshot for a player at hand completion.
     *
     * @param hand the best 5-card hand from hole and community cards
     * @param result the evaluated hand result
     * @param id the player's unique identifier
     */
    public PlayerResult(List<Card> hand, HandResult result, PlayerId id) {
        this.hand = hand;
        this.result = result;
        this.playerId = id;
    }

    /**
     * Returns the hole cards held by this player at showdown.
     *
     * @return the player's hole card list
     */
    public List<Card> getHand() { return hand; }

    /**
     * Returns the evaluated hand result for this player.
     *
     * @return the {@link HandResult} describing the best 5-card hand
     */
    public HandResult getResult() { return result; }

    /**
     * Returns the unique identifier of the player who achieved this result.
     *
     * @return the player's {@link PlayerId}
     */
    public PlayerId getPlayerId() { return playerId; }

    /**
     * Returns a human-readable summary of this result including the winning hand description.
     *
     * @return string in the format "Player wins with {handDescription}"
     */
    public @Override String toString(){
        return "Player wins with " + result.toString();
    }
}