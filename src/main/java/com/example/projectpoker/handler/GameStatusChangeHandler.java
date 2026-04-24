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

        switch (evt.getPropertyName()) {

            case "gameStatus":
            case "state":
                viewUpdater.onGameStatusChanged(
                        (GameStatus) evt.getNewValue()
                );
                break;

            case "blindSize":
                viewUpdater.onBlindSizeChanged(
                        (int) evt.getNewValue()
                );
                break;

            case "round":
                viewUpdater.onRoundCreation(
                        (Round) evt.getNewValue()
                );
                break;

            case "players":
                viewUpdater.onPlayerChange(
                        (ArrayList<Player>) evt.getNewValue(),
                        (ArrayList<Player>) evt.getOldValue()
                );
                break;

            default:
                System.out.println(
                        "Unhandled property: "
                                + evt.getPropertyName()
                );
        }
    }}
