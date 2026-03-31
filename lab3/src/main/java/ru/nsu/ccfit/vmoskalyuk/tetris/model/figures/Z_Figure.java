package ru.nsu.ccfit.vmoskalyuk.tetris.model.figures;

import ru.nsu.ccfit.vmoskalyuk.tetris.model.AbstractTetrisFigure;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.TetrisFigure;

public class Z_Figure extends AbstractTetrisFigure {

    public Z_Figure() {
        super(new int[][][] {
                {{0,0}, {1,0}, {1,1}, {2,1}},
                {{1,0}, {0,1}, {1,1}, {0,2}}
        }, 5); // red
    }

    @Override
    protected TetrisFigure createRotated(int newIndex) {
        Z_Figure rotated = new Z_Figure();
        rotated.rotationIndex = newIndex;
        rotated.setPosition(this.x, this.y);
        return rotated;
    }
}