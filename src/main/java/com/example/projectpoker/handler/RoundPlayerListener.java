package com.example.projectpoker.handler;

import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Roles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RoundPlayerListener implements PropertyChangeListener {
    private Round round;

    public RoundPlayerListener() {
    }
    public RoundPlayerListener(Round round) {
        this.round = round;
    }

    public Round getRound() { return round; }

    public void setRound(Round round) { this.round = round; }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "action" -> playerActionHandler(evt);

        }
    }

    private void playerActionHandler(PropertyChangeEvent evt) {
        switch (evt.getNewValue()) {
            case Action.FORFEIT -> round.removeForfeited();
            default -> throw new IllegalStateException("Unexpected value: " + evt.getNewValue());
        }
    }
}
