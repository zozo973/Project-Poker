package com.example.projectpoker.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class GameStatusChangeHandler implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("gameStatus")) {
            System.out.println("Caught gameStatus change " + evt.getOldValue() + " to " + evt.getNewValue());
        }
    }
}
