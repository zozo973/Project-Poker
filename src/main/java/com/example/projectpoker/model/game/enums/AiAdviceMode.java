package com.example.projectpoker.model.game.enums;

/**
 * Enumeration representing the aggressiveness mode for AI advice or player guidance.
 * Ranges from conservative play to high-risk strategies.
 */
public enum AiAdviceMode {
    /** Conservative playing strategy with fold-heavy decisions. */
    RISKY, 
    /** Balanced strategy following poker fundamentals. */
    NORMAL, 
    /** Tight, cautious strategy emphasizing strong hands only. */
    SAFE
}
