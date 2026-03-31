package ru.nsu.ccfit.vmoskalyuk.tetris.model.figures;

import ru.nsu.ccfit.vmoskalyuk.tetris.model.AbstractTetrisFigure;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.TetrisFigure;

public class J_Figure extends AbstractTetrisFigure {

    public J_Figure() {
        super(new int[][][] {
                {{0,0}, {0,1}, {1,1}, {2,1}},
                {{0,0}, {1,0}, {0,1}, {0,2}},
                {{0,0}, {1,0}, {2,0}, {2,1}},
                {{1,0}, {1,1}, {0,2}, {1,2}}
        }, 6); // blue
    }

    @Override
    protected TetrisFigure createRotated(int newIndex) {
        J_Figure rotated = new J_Figure();
        rotated.rotationIndex = newIndex;
        rotated.setPosition(this.x, this.y);
        return rotated;
    }
}