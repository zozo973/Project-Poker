package com.example.projectpoker.handler;

import com.example.projectpoker.model.game.AiPlayer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PlayerStatusChangeHandler implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("isTurn") ) {
            if (evt.getSource()instanceof AiPlayer) {
                aiPlayerIsTurn(evt);
            } else {
                userIsTurn(evt);
            }

        } else if (evt.getPropertyName().equals("balance")) {
            int newVal = (int) evt.getNewValue();
            int oldVal = (int) evt.getOldValue();
            if (newVal > oldVal) {
                int winnings = newVal - oldVal;
                System.out.println("You just won the round congratulations you won "+ winnings + " from that round");
            }
            // Update users balance display and slider

        }
    }

    private void aiPlayerIsTurn(PropertyChangeEvent evt) {
        // let Ai player make there bet
    }


    private void userIsTurn(PropertyChangeEvent evt) {
        // Make users buttons userable.
    }
}
