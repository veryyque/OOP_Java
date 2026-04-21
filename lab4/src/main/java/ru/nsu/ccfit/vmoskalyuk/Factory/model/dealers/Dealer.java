// ru/nsu/ccfit/vmoskalyuk/Factory/model/dealers/Dealer.java
package ru.nsu.ccfit.vmoskalyuk.Factory.model.dealers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Car;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class Dealer implements Runnable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Storage<Car> carStorage;
    private final int number;
    private final Consumer<SaleEvent> saleCallback;
    private final PrintWriter logWriter;
    private final boolean saleLoggingEnabled;

    private volatile int delayMs;

    public Dealer(Storage<Car> carStorage, int number, Consumer<SaleEvent> saleCallback, int initialDelay, PrintWriter logWriter, boolean saleLoggingEnabled) {
        this.carStorage = carStorage;
        this.number = number;
        this.saleCallback = saleCallback;
        this.logWriter = logWriter;
        this.saleLoggingEnabled = saleLoggingEnabled;
        this.delayMs = Math.max(10, initialDelay);
    }

    public void setDelay(int delayMs) {
        this.delayMs = Math.max(10, delayMs);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Car car = carStorage.take();

                if (car == null) continue;

                logSale(car);
                if (saleCallback != null) {
                    saleCallback.accept(new SaleEvent(number, car));
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

    public record SaleEvent(int dealerNumber, Car car) {
    }
}