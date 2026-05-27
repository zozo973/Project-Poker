package com.example.projectpoker.model.game.enums;

/**
 * Enumeration of poker table positions and special roles.
 * Defines players' positions, blind obligations, and win status.
 */
public enum Roles {
    /** Standard player with no special role. */
    PLAYER(0,"Player"),
    /** The dealer position (typically has no blind obligation). */
    DEALER(0,"Dealer"),
    /** Big blind position (posts 1× the blind size). */
    BIGBLIND(1, "Big Blind"),
    /** Small blind position (posts 0.5× the blind size). */
    SMALLBLIND(0.5, "Small blind"),
    /** Player who won the most recent round. */
    WINNER(0,"Round Winner");

    private final double blindMultiplier;
    private final String description;

    Roles(double blindMultiplier, String description) {
        this.blindMultiplier = blindMultiplier;
        this.description = description;
    }

    /**
     * Returns the blind multiplier for this role.
     * Used to calculate the chip amount a player posts as a blind:
     * SMALLBLIND = 0.5×, BIGBLIND = 1×, all others = 0.
     *
     * @return the multiplier as a double (0, 0.5, or 1.0)
     */
    public double getBlindMultiplier() { return this.blindMultiplier; }

    /**
     * Returns the human-readable description of this role.
     *
     * @return the role description string (e.g. "Dealer", "Big Blind", "Round Winner")
     */
    public String getDescription() { return this.description; }
}
