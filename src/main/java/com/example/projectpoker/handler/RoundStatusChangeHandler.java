package com.example.projectpoker.handler;

import com.example.projectpoker.controller.RoundViewUpdater;
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.enums.RoundStatus;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;



public class RoundStatusChangeHandler implements PropertyChangeListener {
    private final RoundViewUpdater viewUpdater;
    private final Game game;
    public RoundStatusChangeHandler(
            RoundViewUpdater viewUpdater,
            Game game
    ) {
        this.viewUpdater = viewUpdater;
        this.game = game;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "pots" -> viewUpdater.onPotChanged((int) evt.getNewValue());
            case "state" -> executeStateUpdate(evt);
            case "communityCards" -> viewUpdater.onCommunityCardsChanged((ArrayList<Card>) evt.getNewValue(), (ArrayList<Card>) evt.getOldValue());
            case "toPlay" -> viewUpdater.onToPlayChange((int) evt.getNewValue());
            //    Yet to implement
            //    case "betType"; this could display some text on the ui
        }
    }

    private void executeStateUpdate(PropertyChangeEvent evt) {

        RoundStatus status =
                (RoundStatus) evt.getNewValue();

        // Always update phase label
        viewUpdater.onRoundStatusChanged(status.name());
        switch (status) {

            case UNINITIALISED ->
                    viewUpdater.onRoundStarted();

            case DEAL ->
                    viewUpdater.onDealCards();

            case FLOP,
                 TURN,
                 RIVER ->
                    viewUpdater.onCommunityCardsChanged(
                            game.getRound().getCommunityCards(),
                            null
                    );
        }
    }
}


