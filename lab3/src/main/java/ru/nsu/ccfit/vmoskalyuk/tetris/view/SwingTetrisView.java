package ru.nsu.ccfit.vmoskalyuk.tetris.view;

import ru.nsu.ccfit.vmoskalyuk.tetris.controller.GameController;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.*;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.Observer;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SwingTetrisView extends JFrame implements Observer<GameState> {

    private final GameController controller;
    private final JPanel[][] cells = new JPanel[GameModel.ROWS][GameModel.COLS];
    private Timer fallTimer;

    public SwingTetrisView(GameController controller) {
        this.controller = controller;
        controller.registerObserver(this);

        setTitle("Тетрис");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        //меню
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Игра");

        JMenuItem newGame = new JMenuItem("Новая игра");
        newGame.addActionListener(_ -> controller.newGame());

        JMenuItem highScores = new JMenuItem("Рекорды");
        highScores.addActionListener(_ -> showHighScores());

        JMenuItem about = new JMenuItem("О программе");
        about.addActionListener(_ -> JOptionPane.showMessageDialog(this, "Тетрис MVC\nSwing + Observer + GridLayout", "О программе", JOptionPane.INFORMATION_MESSAGE));

        JMenuItem exit = new JMenuItem("Выход");
        exit.addActionListener(_ -> System.exit(0));

        menu.add(newGame);
        menu.add(highScores);
        menu.add(about);
        menu.add(exit);
        bar.add(menu);
        setJMenuBar(bar);

        //поле
        JPanel grid = new JPanel(new GridLayout(GameModel.ROWS, GameModel.COLS, 1, 1));
        grid.setBackground(Color.BLACK);

        for (int r = 0; r < GameModel.ROWS; r++) {
            for (int c = 0; c < GameModel.COLS; c++) {
                JPanel cell = new JPanel();
                cell.setBackground(Color.DARK_GRAY);
                cells[r][c] = cell;
                grid.add(cell);
            }
        }

        add(grid);
        pack();
        setLocationRelativeTo(null);

        grid.setFocusable(true);
        grid.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (controller.getModel().isGameOver()) return;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT  -> controller.moveLeft();
                    case KeyEvent.VK_RIGHT -> controller.moveRight();
                    case KeyEvent.VK_DOWN  -> controller.moveDown();
                    case KeyEvent.VK_UP    -> controller.rotate();
                    case KeyEvent.VK_SPACE -> controller.hardDrop();
                }
            }
        });
        startFallTimer();
    }

    @Override
    public void update(GameState state) {
        SwingUtilities.invokeLater(() -> {
            //перерисовка
            for (int r = 0; r < GameModel.ROWS; r++) {
                for (int c = 0; c < GameModel.COLS; c++) {
                    Color color = Color.DARK_GRAY;
                    if (state.board[r][c] != 0) color = getColor(state.board[r][c]);

                    if (state.currentFigure != null) {
                        for (int[] b : state.currentFigure.getBlocks()) {
                            if (state.currentFigure.getX() + b[0] == c && state.currentFigure.getY() + b[1] == r) {
                                color = getColor(state.currentFigure.getColor());
                                break;
                            }
                        }
                    }
                    cells[r][c].setBackground(color);
                }
            }

            if (state.isGameOver()) {
                int sc = state.getScore();
                if (sc > 0) {
                    String name = JOptionPane.showInputDialog(
                            this,
                            "Игра окончена!\nВаш счёт: " + sc + "\nВведите имя:",
                            "Новый рекорд?",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    //убрать доступ к модели
                    if (name != null && !name.trim().isEmpty()) {
                        controller.getModel().addHighScore(sc);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Игра окончена! Счёт: " + sc);
                }
            }
        });
    }

    private Color getColor(int id) {
        return switch (id) {
            case 1 -> Color.CYAN;
            case 2 -> Color.YELLOW;
            case 3 -> Color.MAGENTA;
            case 4 -> Color.GREEN;
            case 5 -> Color.RED;
            case 6 -> Color.BLUE;
            case 7 -> new Color(255, 140, 0);
            default -> Color.GRAY;
        };
    }
    private void startFallTimer() {
        fallTimer = new Timer(650, _ -> {
            if (!controller.getModel().isGameOver()) {
                controller.moveDown();
            } else {
                fallTimer.stop();
            }
        });
        fallTimer.start();
    }
    private void showHighScores() {
        StringBuilder sb = new StringBuilder("Рекорды:\n\n");
        List<Integer> scores = controller.getModel().getHighScores();
        if (scores.isEmpty()) sb.append("Пока нет");
        else for (int i = 0; i < scores.size(); i++) sb.append(i+1).append(") ").append(scores.get(i)).append("\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Рекорды", JOptionPane.PLAIN_MESSAGE);
    }
}