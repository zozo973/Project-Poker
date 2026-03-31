package com.example.projectpoker.model.game.statemachine;

public class GameContext {
    private IPokerGameState currentState;

    public void setState(IPokerGameState state) {
        this.currentState = state;
    }

    public void event() {
        currentState.handleEvent();
    }
}
