package com.example.projectpoker.handler;

import com.example.projectpoker.controller.RoundViewUpdater;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.enums.GameStatus;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class GameStatusChangeHandler implements PropertyChangeListener {

    private final RoundViewUpdater viewUpdater;

    public GameStatusChangeHandler(RoundViewUpdater viewUpdater) {
        this.viewUpdater = viewUpdater;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()){
            case "gameStatus" -> viewUpdater.onGameStatusChanged((GameStatus) evt.getNewValue());
            case "blindSize" -> viewUpdater.onBlindSizeChanged((int) evt.getNewValue());
            case "round" -> viewUpdater.onRoundCreation((Round) evt.getNewValue());
            case "players" -> viewUpdater.onPlayerChange((ArrayList<Player>) evt.getNewValue(), (ArrayList<Player>) evt.getOldValue());
            default -> throw new IllegalStateException("Unexpected value: " + evt.getPropertyName());
        }
    }
}
