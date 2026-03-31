package ru.nsu.ccfit.vmoskalyuk.tetris.model;

public interface Observer<C> {
    void update(C context);
}