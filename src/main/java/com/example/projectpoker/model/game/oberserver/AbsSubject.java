package com.example.projectpoker.model.game.oberserver;

import java.util.ArrayList;

public abstract class AbsSubject implements Subject {

    private ArrayList<Observer> observers;

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
}
