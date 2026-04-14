package ru.nsu.ccfit.vmoskalyuk.Factory.model.observable;

import java.util.ArrayList;
import java.util.List;

public class Observable {
    private final List<Observer> observers = new ArrayList<>();
    private final Object mutex = new Object();
    private boolean changed = false;

    public void addObserver(Observer observer) {
        if (observer == null) return;
        synchronized (mutex) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    public void deleteObserver(Observer observer) {
        synchronized (mutex) {
            observers.remove(observer);
        }
    }

    public void setChanged() {
        synchronized (mutex) {
            changed = true;
        }
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    public void notifyObservers(Object arg) {
        List<Observer> observersCopy;

        synchronized (mutex) {
            if (!changed) return;
            observersCopy = new ArrayList<>(observers);
            changed = false;
        }

        for (Observer observer : observersCopy) {
            observer.update(this, arg);
        }
    }
}