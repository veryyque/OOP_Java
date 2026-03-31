package ru.nsu.ccfit.vmoskalyuk.tetris.model;

import ru.nsu.ccfit.vmoskalyuk.tetris.model.figures.*;

import java.util.Random;

public class FigureFactory {

    private static final Random rnd = new Random();

    public static TetrisFigure createRandom() {
        int type = rnd.nextInt(7);
        return switch (type) {
            case 0 -> new I_Figure();
            case 1 -> new O_Figure();
            case 2 -> new T_Figure();
            case 3 -> new S_Figure();
            case 4 -> new Z_Figure();
            case 5 -> new J_Figure();
            case 6 -> new L_Figure();
            default -> new I_Figure();
        };
    }
}