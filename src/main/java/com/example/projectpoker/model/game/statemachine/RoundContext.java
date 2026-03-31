package com.example.projectpoker.model.game.statemachine;

public class RoundContext {
    private IPokerRoundState currentState;

    public void setState(IPokerRoundState state) { this.currentState = state; }

    public void event() { currentState.handleEvent(); }
}
