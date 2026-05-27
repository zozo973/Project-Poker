package com.example.projectpoker.model.game.enums;

/**
 * Enumeration of betting mode modifiers for a round.
 * Controls how the round's betting flow proceeds and concludes.
 */
public enum BetType {
    /** Standard betting rules apply. */
    NORMAL,
    /** Skip directly to showdown, skipping remaining community card deals and betting. */
    SKIP2SHOWDOWN,
    /** A side pot has been created during this round. */
    SIDEPOT,
    /** End the current round immediately. */
    ENDROUND;
}
