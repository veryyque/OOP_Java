package ru.nsu.ccfit.vmoskalyuk.Factory;

import ru.nsu.ccfit.vmoskalyuk.Factory.config.Config;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.CarFactory;
import ru.nsu.ccfit.vmoskalyuk.Factory.controller.FactoryController;
import ru.nsu.ccfit.vmoskalyuk.Factory.view.FactoryView;

import javax.swing.*;

public class FactoryApp {
    public static void main(String[] args) {
        System.out.println("STARTING...");
        SwingUtilities.invokeLater(() -> {
            try {
                Config config = new Config();

                CarFactory model = new CarFactory(config);

                FactoryView view = new FactoryView();

                FactoryController controller = new FactoryController(model, view);

                controller.start();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Ошибка запуска: " + e.getMessage());
            }
        });
    }
}