package com.example.projectpoker.handler;

import com.example.projectpoker.controller.RoundViewUpdater;
import com.example.projectpoker.model.Hand;
import com.example.projectpoker.model.game.AiPlayer;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Roles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PlayerStatusChangeHandler implements PropertyChangeListener {
    private final RoundViewUpdater viewUpdater;

    public PlayerStatusChangeHandler(RoundViewUpdater viewUpdater) {
        this.viewUpdater = viewUpdater;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "isTurn" -> handleTurnChange(evt);
            case "balance" -> handleBalanceChange(evt);
            case "action" -> viewUpdater.onPlayerActionChange((Action) evt.getNewValue());
            case "role" -> viewUpdater.onPlayerRoleUpdate((Roles) evt.getNewValue());
        }
    }

    private void handleTurnChange(PropertyChangeEvent evt) {
        boolean isTurn = (boolean) evt.getNewValue();
        if (!isTurn) return;

        if (evt.getSource() instanceof AiPlayer) {
            viewUpdater.onAiTurnStarted();
        } else if (evt.getSource() instanceof Player player) {
            viewUpdater.onUserTurnStarted();
        }
    }

    private void handleBalanceChange(PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof Player player)) return;
        if ((evt.getSource() instanceof AiPlayer)) return;

        int oldVal = (int) evt.getOldValue();
        int newVal = (int) evt.getNewValue();

        viewUpdater.onBalanceChanged(player, oldVal, newVal);
    }
}