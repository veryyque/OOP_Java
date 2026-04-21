// ru/nsu/ccfit/vmoskalyuk/Factory/model/suppliers/Supplier.java
package ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Detail;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Supplier<T extends Detail> implements Runnable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    protected final Storage<T> storage;
    protected final String supplierType;
    protected final PrintWriter logWriter;

    protected volatile int delayMs;

    public Supplier(Storage<T> storage, int delayMs, String supplierType, PrintWriter logWriter) {
        this.storage = storage;
        this.delayMs = Math.max(10, delayMs);
        this.supplierType = supplierType;
        this.logWriter = logWriter;
    }

    protected abstract T createDetail();

    public void setDelay(int delayMs) {
        this.delayMs = Math.max(10, delayMs);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                T detail = createDetail();

                logCreation(detail);

                storage.put(detail);

                Thread.sleep(delayMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void logCreation(T detail) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String logLine = String.format("%s [%s] CREATED %s #%d",
                timestamp, supplierType, detail.getClass().getSimpleName(), detail.getId());

        synchronized (logWriter) {
            logWriter.println(logLine);
            logWriter.flush();
        }
    }
}