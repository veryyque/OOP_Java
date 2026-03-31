package ru.nsu.ccfit.vmoskalyuk.tetris.model;

public class GameState {
    public final int[][] board;
    public final TetrisFigure currentFigure;
    public final int score;
    public final boolean gameOver;

    public GameState(int[][] board, TetrisFigure currentFigure, int score, boolean gameOver) {
        this.board = board;
        this.currentFigure = currentFigure;
        this.score = score;
        this.gameOver = gameOver;
    }

    public int[][] getBoard() {
        return board;
    }

    public TetrisFigure getCurrentFigure() {
        return currentFigure;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}