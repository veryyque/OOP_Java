package ru.nsu.ccfit.vmoskalyuk.tetris.controller;

import ru.nsu.ccfit.vmoskalyuk.tetris.model.GameModel;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.GameState;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.Observer;

public class GameController {

    private final GameModel model;

    public GameController(GameModel model) {
        this.model = model;
    }

    public void registerObserver(Observer<GameState> observer) {
        model.addObserver(observer);
    }

    public void moveLeft() {
        model.moveLeft();
    }

    public void moveRight() {
        model.moveRight();
    }

    public void rotate() {
        model.rotate();
    }

    public void moveDown() {
        model.moveDown();
    }

    public void hardDrop() {
        while (canMoveDown()) {
            moveDown();
        }

        model.placePiece();
        model.clearLines();
        model.spawnNewPiece();
    }

    private boolean canMoveDown() {
        int oldY = model.getCurrent().getY();
        model.getCurrent().setPosition(model.getCurrent().getX(), oldY + 1);
        boolean can = model.canPlace(model.getCurrent());
        model.getCurrent().setPosition(model.getCurrent().getX(), oldY);
        return can;
    }

    public void newGame() {
        model.newGame();
    }

    public GameModel getModel() {
        return model;
    }
}