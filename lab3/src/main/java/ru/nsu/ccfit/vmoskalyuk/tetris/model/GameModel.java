package ru.nsu.ccfit.vmoskalyuk.tetris.model;

import java.io.*;
import java.util.*;

public class GameModel extends Observable<GameState> {

    public static final int COLS = 10;
    public static final int ROWS = 20;

    private int[][] board = new int[ROWS][COLS];
    private TetrisFigure current;
    private int score = 0;
    private boolean gameOver = false;

    private final List<Integer> highScores = new ArrayList<>();

    public GameModel() {
        loadHighScores();
        spawnNewPiece();
    }

    public void newGame() {
        board = new int[ROWS][COLS];
        score = 0;
        gameOver = false;
        spawnNewPiece();
    }

    public void spawnNewPiece() {
        current = FigureFactory.createRandom();
        current.setPosition(COLS / 2 - 2, 0); //позиционирую по центру

        if (!canPlace(current)) {
            gameOver = true;
        }
        notifyState();
    }

    public boolean canPlace(TetrisFigure piece) {
        for (int[] b : piece.getBlocks()) {
            int x = piece.getX() + b[0];
            int y = piece.getY() + b[1];
            if (x < 0 || x >= COLS || y >= ROWS) return false;
            if (y >= 0 && board[y][x] != 0) return false;
        }
        return true;
    }

    public void moveLeft() {
        current.setPosition(current.getX() - 1, current.getY());
        if (!canPlace(current)) current.setPosition(current.getX() + 1, current.getY());
        notifyState();
    }

    public void moveRight() {
        current.setPosition(current.getX() + 1, current.getY());
        if (!canPlace(current)) current.setPosition(current.getX() - 1, current.getY());
        notifyState();
    }

    public void rotate() {
        TetrisFigure rotated = current.rotate();
        if (canPlace(rotated)) current = rotated;
        notifyState();
    }

    public void moveDown() {
        current.setPosition(current.getX(), current.getY() + 1);
        if (!canPlace(current)) {
            current.setPosition(current.getX(), current.getY() - 1);
            placePiece();
            clearLines();
            spawnNewPiece();
        }
        notifyState();
    }

    public void placePiece() {
        for (int[] b : current.getBlocks()) {
            int x = current.getX() + b[0];
            int y = current.getY() + b[1];
            if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                board[y][x] = current.getColor();
            }
        }
    }

    public void clearLines() {
        int lines = 0;
        for (int y = ROWS - 1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < COLS; x++) {
                if (board[y][x] == 0) full = false;
            }
            if (full) {
                lines++;
                for (int yy = y; yy > 0; yy--) {
                    System.arraycopy(board[yy-1], 0, board[yy], 0, COLS);
                }
                Arrays.fill(board[0], 0);
                y++;
            }
        }
        score += lines * lines * 100;
    }

    public void notifyState() {
        GameState state = new GameState(board, current, score, gameOver);
        notify(state);
    }
    public GameState getCurrentState() {
        return new GameState(
                board,
                current,//текущая фигура
                score,
                gameOver
        );
    }

    public TetrisFigure getCurrent() { return current; }
    public boolean isGameOver() { return gameOver; }
    public List<Integer> getHighScores() { return new ArrayList<>(highScores); }

    public void addHighScore(int sc) {
        if (sc > 0) {
            highScores.add(sc);
            highScores.sort(Collections.reverseOrder());
            while (highScores.size() > 10) highScores.removeLast();
            saveHighScores();
        }
    }

    private void loadHighScores() {
        try (BufferedReader br = new BufferedReader(new FileReader("highscores.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                highScores.add(Integer.parseInt(line.trim()));
            }
        } catch (Exception ignored) {}
    }

    private void saveHighScores() {
        try (PrintWriter pw = new PrintWriter("highscores.txt")) {
            for (int s : highScores) pw.println(s);
        } catch (Exception ignored) {}
    }
}