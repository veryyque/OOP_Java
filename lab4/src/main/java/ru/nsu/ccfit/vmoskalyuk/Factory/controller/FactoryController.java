package ru.nsu.ccfit.vmoskalyuk.Factory.controller;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.CarFactory;
import ru.nsu.ccfit.vmoskalyuk.Factory.view.FactoryView;

import javax.swing.*;

public class FactoryController {

    private final CarFactory model;
    private final FactoryView view;

    private final Timer refreshTimer;

    public FactoryController(CarFactory model, FactoryView view) {
        this.model = model;
        this.view = view;

        //ссылка на статический метод класса для обновления интерфейса
        model.addListener(this::updateView);

        view.setInitialDelays(model.getInitialBodyDelay(), model.getInitialMotorDelay(), model.getInitialAccessoryDelay(), model.getInitialDealerDelay());

        view.setBodySpeedListener(model::setBodyDelay);
        view.setMotorSpeedListener(model::setMotorDelay);
        view.setAccessorySpeedListener(model::setAccessoryDelay);
        view.setDealerSpeedListener(model::setDealerDelay);

        view.setCloseListener(this::handleWindowClosing);

        this.refreshTimer = new Timer(100, _ -> updateView());
    }

    private void handleWindowClosing() {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Завершить работу фабрики?",
                "Выход",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            model.shutdown();
            refreshTimer.stop();
            view.dispose();
            System.exit(0);
        }
    }

    private void updateView() {
        SwingUtilities.invokeLater(() -> view.updateStats(
                model.getBodyCount(),
                model.getMotorCount(),
                model.getAccessoryCount(),
                model.getCarCount(),
                model.getTotalBodiesProduced(),
                model.getTotalMotorsProduced(),
                model.getTotalAccessoriesProduced(),
                model.getTotalCarsProduced(),
                model.getPendingTasks()
        ));
    }

    public void start() {
        model.start();
        refreshTimer.start();
        view.setVisible(true);
    }
}