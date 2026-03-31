package ru.nsu.ccfit.vmoskalyuk.tetris.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Observable<C> {
    private final List<Observer<C>> observers = new ArrayList<>();

    public void addObserver(Observer<C> observer) {
        observers.add(observer);
    }

    protected void notify(C context) {
        observers.forEach(o -> o.update(context));
    }
}