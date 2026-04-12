package com.example.projectpoker.handler;

import com.example.projectpoker.controller.RoundViewUpdater;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class GameStatusChangeHandler implements PropertyChangeListener {

    private final RoundViewUpdater viewUpdater;

    public GameStatusChangeHandler(RoundViewUpdater viewUpdater) {
        this.viewUpdater = viewUpdater;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()){
            case "gameStatus" ->gameStatusChange(evt);
        }



    }

    private void gameStatusChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("gameStatus")) {
            System.out.println("Caught gameStatus change " + evt.getOldValue() + " to " + evt.getNewValue());
        }
    }
}
