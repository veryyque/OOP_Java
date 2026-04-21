package ru.nsu.ccfit.vmoskalyuk.Factory.model.controller;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Car;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

import java.util.function.IntSupplier;

public class StorageController extends Thread {

    private final Storage<Car> carStorage;
    private final Runnable requestAssemblyCallback; //
    private final IntSupplier pendingTasksSupplier; //функциональный интерфейс

    public StorageController(Storage<Car> carStorage, Runnable requestAssemblyCallback, IntSupplier pendingTasksSupplier) {
        this.carStorage = carStorage;
        this.requestAssemblyCallback = requestAssemblyCallback;
        this.pendingTasksSupplier = pendingTasksSupplier;
        setName("StorageController");
    }

    private void orderMissingCarsIfNeeded() {
        int missingCars = carStorage.getMaxSize() - carStorage.size() - pendingTasksSupplier.getAsInt();
        for (int i = 0; i < missingCars; i++) requestAssemblyCallback.run();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                orderMissingCarsIfNeeded();
                synchronized (carStorage) {
                    carStorage.wait();
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        synchronized (carStorage) {
            carStorage.notifyAll();
        }
        interrupt();
    }
}