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
            case "blindSize" -> viewUpdater.onBlindSizeChanged((int) evt.getNewValue());
            case "pot" -> viewUpdater.onPotChanged((int) evt.getNewValue());
            case "phase" -> viewUpdater.onRoundPhaseChanged((String) evt.getNewValue());
        }
    }

}
