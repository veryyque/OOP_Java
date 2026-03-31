package ru.nsu.ccfit.vmoskalyuk.tetris.model;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTetrisFigure implements TetrisFigure {

    protected int x = 0;
    protected int y = 0;
    protected final int color;
    protected final int[][][] rotations;
    protected int rotationIndex = 0;

    protected AbstractTetrisFigure(int[][][] rotations, int color) {
        this.rotations = rotations;
        this.color = color;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public List<int[]> getBlocks() {
        List<int[]> blocks = new ArrayList<>();
        int[][] current = rotations[rotationIndex];
        for (int[] pos : current) {
            blocks.add(new int[]{pos[0], pos[1]});
        }
        return blocks;
    }

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public TetrisFigure rotate() {
        int nextIndex = (rotationIndex + 1) % rotations.length;
        return createRotated(nextIndex);
    }

    protected abstract TetrisFigure createRotated(int newIndex);

}