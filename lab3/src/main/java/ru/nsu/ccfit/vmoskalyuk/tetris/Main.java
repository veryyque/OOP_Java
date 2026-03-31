package ru.nsu.ccfit.vmoskalyuk.tetris;

import ru.nsu.ccfit.vmoskalyuk.tetris.controller.GameController;
import ru.nsu.ccfit.vmoskalyuk.tetris.model.GameModel;
import ru.nsu.ccfit.vmoskalyuk.tetris.view.ConsoleTetrisView;
import ru.nsu.ccfit.vmoskalyuk.tetris.view.SwingTetrisView;

import javax.swing.*;

public class Main {
     static void main() {
        String mode = JOptionPane.showInputDialog(null,
                "Выберите режим:\n1 — Графический интерфейс\n2 — Консольный режим",
                "Тетрис", JOptionPane.QUESTION_MESSAGE);

        GameModel model = new GameModel();
        GameController controller = new GameController(model);

        if ("2".equals(mode)) {
            new ConsoleTetrisView(controller).start();
        } else {

            SwingUtilities.invokeLater(() -> {
                SwingTetrisView view = new SwingTetrisView(controller);
                view.setVisible(true);
            });
        }
    }
}