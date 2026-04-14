package ru.nsu.ccfit.vmoskalyuk.Factory.model.observable;

public interface Observer {
    void update(Observable o, Object arg);
}