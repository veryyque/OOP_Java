package ru.nsu.ccfit.vmoskalyuk.tetris.model.figures;

import ru.nsu.ccfit.vmoskalyuk.tetris.model.AbstractTetrisFigure;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.TetrisFigure;

public class T_Figure extends AbstractTetrisFigure {

    public T_Figure() {
        super(new int[][][] {
                {{0,0}, {1,0}, {2,0}, {1,1}},
                {{1,0}, {0,1}, {1,1}, {1,2}},
                {{1,0}, {0,1}, {1,1}, {2,1}},
                {{0,0}, {0,1}, {1,1}, {0,2}}
        }, 3); // magenta
    }

    @Override
    protected TetrisFigure createRotated(int newIndex) {
        T_Figure rotated = new T_Figure();
        rotated.rotationIndex = newIndex;
        rotated.setPosition(this.x, this.y);
        return rotated;
    }
}