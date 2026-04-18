// ru/nsu/ccfit/vmoskalyuk/Factory/model/dealers/Dealer.java
package ru.nsu.ccfit.vmoskalyuk.Factory.model.dealers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Car;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observer;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Dealer implements Runnable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Storage<Car> carStorage;
    private final int number;
    private final Observer saleObserver;
    private final PrintWriter logWriter;        // единый лог от CarFactory
    private final boolean saleLoggingEnabled;

    private volatile int delayMs;
    private volatile boolean running = true;

    public Dealer(Storage<Car> carStorage, int number, Observer saleObserver,
                  int initialDelay, PrintWriter logWriter, boolean saleLoggingEnabled) {
        this.carStorage = carStorage;
        this.number = number;
        this.saleObserver = saleObserver;
        this.logWriter = logWriter;
        this.saleLoggingEnabled = saleLoggingEnabled;
        this.delayMs = Math.max(10, initialDelay);
    }

    public void setDelay(int delayMs) {
        this.delayMs = Math.max(10, delayMs);
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Car car = carStorage.take();

                if (car == null) continue;

                logSale(car);

                // Уведомляем CarFactory через Observer
                if (saleObserver != null) {
                    saleObserver.update(null, new SaleEvent(number, car));
                }

                Thread.sleep(delayMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void logSale(Car car) {
        if (!saleLoggingEnabled) {
            return;
        }
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String logLine = String.format("%s: Dealer %d: Auto %d (Body: %d, Motor: %d, Accessory: %d)",
                timestamp, number, car.getId(),
                car.getBody().getId(), car.getMotor().getId(), car.getAccessory().getId());

        synchronized (logWriter) {
            logWriter.println(logLine);
            logWriter.flush();
        }
    }

    public void shutdown() {
        running = false;
    }

    public static class SaleEvent {
        public final int dealerNumber;
        public final Car car;

        public SaleEvent(int dealerNumber, Car car) {
            this.dealerNumber = dealerNumber;
            this.car = car;
        }
    }
}