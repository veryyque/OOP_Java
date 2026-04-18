package ru.nsu.ccfit.vmoskalyuk.Factory.model.controller;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Car;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observer;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observable;

import java.util.function.IntSupplier;

public class StorageController extends Thread implements Observer {

    private final Storage<Car> carStorage;
    private final Runnable requestAssemblyCallback;
    private final IntSupplier pendingTasksSupplier;
    private final Object signalLock = new Object();
    private volatile boolean running = true;
    private boolean hasSignal = false;

    public StorageController(Storage<Car> carStorage,
                             Runnable requestAssemblyCallback,
                             IntSupplier pendingTasksSupplier) {
        this.carStorage = carStorage;
        this.requestAssemblyCallback = requestAssemblyCallback;
        this.pendingTasksSupplier = pendingTasksSupplier;
        setName("StorageController");

        carStorage.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if ("take".equals(arg)) {
            synchronized (signalLock) {
                hasSignal = true;
                signalLock.notifyAll();
            }
        }
    }

    private void orderMissingCarsIfNeeded() {
        int missingCars = carStorage.getMaxSize() - carStorage.size() - pendingTasksSupplier.getAsInt();
        for (int i = 0; i < missingCars; i++) {
            requestAssemblyCallback.run();
        }
    }

    @Override
    public void run() {
        orderMissingCarsIfNeeded();

        while (running && !isInterrupted()) {
            try {
                synchronized (signalLock) {
                    while (running && !hasSignal) {
                        signalLock.wait();
                    }
                    hasSignal = false;
                }
                if (!running || isInterrupted()) {
                    break;
                }
                orderMissingCarsIfNeeded();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        synchronized (signalLock) {
            hasSignal = true;
            signalLock.notifyAll();
        }
        interrupt();
        carStorage.deleteObserver(this);
    }
}