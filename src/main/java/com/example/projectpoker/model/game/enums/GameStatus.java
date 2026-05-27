package com.example.projectpoker.model.game.enums;

/**
 * Enumeration representing the lifecycle status of a poker game.
 * Tracks transitions from initial setup through play to conclusion.
 */
public enum GameStatus {
    /** Game has been configured but rounds have not started yet. */
    INITIALISED,
    /** Game is currently in progress with active rounds. */
    RUNNING,
    /** Game has concluded and no more rounds will be played. */
    ENDED;
}
