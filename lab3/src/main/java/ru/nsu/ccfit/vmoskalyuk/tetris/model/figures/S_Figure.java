package ru.nsu.ccfit.vmoskalyuk.tetris.model.figures;

import ru.nsu.ccfit.vmoskalyuk.tetris.model.AbstractTetrisFigure;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.TetrisFigure;

public class S_Figure extends AbstractTetrisFigure {

    public S_Figure() {
        super(new int[][][] {
                {{1,0}, {2,0}, {0,1}, {1,1}},
                {{0,0}, {0,1}, {1,1}, {1,2}}
        }, 4); // green
    }

    @Override
    protected TetrisFigure createRotated(int newIndex) {
        S_Figure rotated = new S_Figure();
        rotated.rotationIndex = newIndex;
        rotated.setPosition(this.x, this.y);
        return rotated;
    }
}