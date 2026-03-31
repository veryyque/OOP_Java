package ru.nsu.ccfit.vmoskalyuk.tetris.model.figures;

import ru.nsu.ccfit.vmoskalyuk.tetris.model.AbstractTetrisFigure;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.TetrisFigure;

public class L_Figure extends AbstractTetrisFigure {

    public L_Figure() {
        super(new int[][][] {
                {{2,0}, {0,1}, {1,1}, {2,1}},
                {{0,0}, {0,1}, {0,2}, {1,2}},
                {{0,0}, {1,0}, {2,0}, {0,1}},
                {{0,0}, {1,0}, {1,1}, {1,2}}
        }, 7); // orange
    }

    @Override
    protected TetrisFigure createRotated(int newIndex) {
        L_Figure rotated = new L_Figure();
        rotated.rotationIndex = newIndex;
        rotated.setPosition(this.x, this.y);
        return rotated;
    }
}