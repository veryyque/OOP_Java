package ru.nsu.ccfit.vmoskalyuk.tetris.model.figures;

import ru.nsu.ccfit.vmoskalyuk.tetris.model.AbstractTetrisFigure;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.TetrisFigure;

public class O_Figure extends AbstractTetrisFigure {

    public O_Figure() {
        super(new int[][][] {
                {{0,0}, {1,0}, {0,1}, {1,1}}
        }, 2); // yellow
    }

    @Override
    protected TetrisFigure createRotated(int newIndex) {
        return new O_Figure(); // O не поворачивается
    }
}