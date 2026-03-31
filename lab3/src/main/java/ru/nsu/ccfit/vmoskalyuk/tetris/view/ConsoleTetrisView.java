package ru.nsu.ccfit.vmoskalyuk.tetris.view;

import ru.nsu.ccfit.vmoskalyuk.tetris.controller.GameController;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleTetrisView implements Observer<GameState> {

    private final GameController controller;

    public ConsoleTetrisView(GameController controller) {
        this.controller = controller;
        controller.registerObserver(this);
    }

    public void start() {
        System.out.println("=== КОНСОЛЬНЫЙ ТЕТРИС ===");
        System.out.println("Управление:");
        System.out.println("  a     - влево");
        System.out.println("  d     - вправо");
        System.out.println("  s     - вниз на один шаг");
        System.out.println("  w     - поворот");
        System.out.println("  -     - мгновенный дроп");
        System.out.println("  q     - выход");
        System.out.println("После каждой команды нажми Enter для обновления экрана");
        System.out.println("===================================\n");

        render(controller.getModel().getCurrentState());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;

                String cmd = line.trim().toLowerCase();
                if (cmd.isEmpty()) {
                    render(controller.getModel().getCurrentState());
                    continue;
                }

                char c = cmd.charAt(0);
                switch (c) {
                    case 'a' -> controller.moveLeft();
                    case 'd' -> controller.moveRight();
                    case 's' -> controller.moveDown();
                    case 'w' -> controller.rotate();
                    case '-' -> controller.hardDrop();
                    case 'q' -> {
                        System.out.println("Выход из игры.");
                        System.exit(0);
                    }
                    default -> System.out.println("Неизвестная команда. Используй a,d,s,w,пробел,q");
                }

                render(controller.getModel().getCurrentState());
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    @Override
    public void update(GameState state) {
        render(state);
    }

    private void render(GameState state) {
        System.out.print("\033[H\033[2J"); //очистить консоль
        System.out.flush();

        int[][] board = state.getBoard();
        TetrisFigure p = state.getCurrentFigure();
        int score = state.getScore();
        boolean gameOver = state.isGameOver();

        System.out.println("СЧЁТ: " + score);
        System.out.println();

        for (int y = 0; y < GameModel.ROWS; y++) {
            System.out.print("|");
            for (int x = 0; x < GameModel.COLS; x++) {
                boolean isPiece = false;
                if (p != null) {
                    for (int[] b : p.getBlocks()) {
                        if (p.getX() + b[0] == x && p.getY() + b[1] == y) {
                            isPiece = true;
                            break;
                        }
                    }
                }
                System.out.print(isPiece ? "*" : (board[y][x] != 0 ? "*" : " "));
            }
            System.out.println("|");
        }
        System.out.println("---------------------");

        if (gameOver) {
            System.out.println("GAME OVER");
        }
    }
}