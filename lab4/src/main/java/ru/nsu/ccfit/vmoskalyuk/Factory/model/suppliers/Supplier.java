package ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Detail;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

public abstract class Supplier<T extends Detail> implements Runnable {
    protected final Storage<T> storage;
    protected final String name;
    protected volatile int delayMs;
    protected volatile boolean running = true;

    public Supplier(Storage<T> storage, int delayMs, String name) {
        this.storage = storage;
        this.delayMs = delayMs;
        this.name = name;
    }

    protected abstract T createDetail();

    public void setDelay(int delayMs) {
        this.delayMs = Math.max(10, delayMs);
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                T detail = createDetail();
                storage.put(detail);
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public String getName() { return name; }
}