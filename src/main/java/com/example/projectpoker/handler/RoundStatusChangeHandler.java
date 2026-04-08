package com.example.projectpoker.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RoundStatusChangeHandler implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("roundStatus")) {
            roundStatusChange(evt);
            System.out.println("Caught roundStatus change " + evt.getOldValue() + " to " + evt.getNewValue());
        }
    }

    private void roundStatusChange(PropertyChangeEvent evt) {

    }


}
