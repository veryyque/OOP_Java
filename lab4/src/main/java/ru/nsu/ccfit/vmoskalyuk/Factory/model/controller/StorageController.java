package ru.nsu.ccfit.vmoskalyuk.Factory.model.controller;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Car;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observer;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observable;

public class StorageController extends Thread implements Observer {

    private final Storage<Car> carStorage;
    private final Runnable requestAssemblyCallback;
    private volatile boolean running = true;

    public StorageController(Storage<Car> carStorage, Runnable requestAssemblyCallback) {
        this.carStorage = carStorage;
        this.requestAssemblyCallback = requestAssemblyCallback;
        setName("StorageController");

        carStorage.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if ("take".equals(arg)) {
            requestAssemblyCallback.run();
        }
    }

    private void orderMissingCars() {
        int freeSlots = carStorage.getMaxSize() - carStorage.size();
        for (int i = 0; i < freeSlots; i++) {
            requestAssemblyCallback.run();
        }
    }

    @Override
    public void run() {
        //заказ изначально на треть от размера склада машин
        int initial = carStorage.getMaxSize() / 3;
        for (int i = 0; i < initial; i++) {
            requestAssemblyCallback.run();
        }

        while (running && !isInterrupted()) {
            try {
                Thread.sleep(2000);
                orderMissingCars(); //на всякий случай
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
        carStorage.deleteObserver(this);
    }
}