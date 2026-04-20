package com.example.projectpoker.handler;

import com.example.projectpoker.controller.RoundViewUpdater;
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.enums.RoundStatus;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class RoundStatusChangeHandler implements PropertyChangeListener {
    private final RoundViewUpdater viewUpdater;

    public RoundStatusChangeHandler(RoundViewUpdater viewUpdater) {
        this.viewUpdater = viewUpdater;
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
        switch(evt.getNewValue()) {
       //     case RoundStatus.END -> viewUpdater.
            default -> viewUpdater.onRoundStatusChanged((String) evt.getNewValue());
        }



    }

}
