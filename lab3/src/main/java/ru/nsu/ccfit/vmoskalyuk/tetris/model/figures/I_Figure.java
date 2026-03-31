package ru.nsu.ccfit.vmoskalyuk.tetris.model.figures;

import ru.nsu.ccfit.vmoskalyuk.tetris.model.AbstractTetrisFigure;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.TetrisFigure;

public class I_Figure extends AbstractTetrisFigure {

    public I_Figure() {
        super(new int[][][] { //вызвать конструктор абстрактного класса фигур
                {{0,0}, {1,0}, {2,0}, {3,0}},
                {{0,0}, {0,1}, {0,2}, {0,3}}
        }, 1);
    }

    @Override
    protected TetrisFigure createRotated(int newIndex) {
        I_Figure rotated = new I_Figure();
        rotated.rotationIndex = newIndex;
        rotated.setPosition(this.x, this.y);
        return rotated;
    }
}