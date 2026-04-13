package com.example.projectpoker.handler;

import com.example.projectpoker.controller.RoundViewUpdater;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RoundStatusChangeHandler implements PropertyChangeListener {
    private final RoundViewUpdater viewUpdater;

    public RoundStatusChangeHandler(RoundViewUpdater viewUpdater) {
        this.viewUpdater = viewUpdater;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "pots" -> viewUpdater.onPotChanged((int) evt.getNewValue());
            case "state" -> viewUpdater.onRoundPhaseChanged((String) evt.getNewValue());
        //    Yet to implement
        //    case "betType";
        //    case "communityCards";
        //    case "toPlay";
        }
    }

}
