package ru.nsu.ccfit.vmoskalyuk.tetris.model;

import java.util.List;

public interface TetrisFigure {

    int getColor();

    List<int[]> getBlocks();

    int getX();
    int getY();

    void setPosition(int x, int y);

    TetrisFigure rotate();

}