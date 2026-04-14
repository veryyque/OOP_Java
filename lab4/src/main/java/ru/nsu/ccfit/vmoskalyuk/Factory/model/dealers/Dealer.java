// ru/nsu/ccfit/vmoskalyuk/Factory/model/dealers/Dealer.java
package ru.nsu.ccfit.vmoskalyuk.Factory.model.dealers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Car;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observer;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Dealer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Dealer.class.getName());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    private final Storage<Car> carStorage;
    private final int number;
    private final Observer saleObserver;

    private volatile int delayMs;
    private volatile boolean running = true;

    public Dealer(Storage<Car> carStorage, int number, Observer saleObserver, int initialDelay) {
        this.carStorage = carStorage;
        this.number = number;
        this.saleObserver = saleObserver;
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

                if (car == null) {
                    break;
                }

                logSale(car);

                //Уведомляем через Observer - передача CarFactory
                if (saleObserver != null) {
                    saleObserver.update(null, new SaleEvent(number, car));
                }

                Thread.sleep(delayMs);

            } catch (InterruptedException e) {
                LOGGER.info("Dealer " + number + " interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void logSale(Car car) {
        String time = TIME_FORMAT.format(new Date());
        String logLine = String.format("%s: Dealer %d: Auto %d (Body: %d, Motor: %d, Accessory: %d)",
                time, number, car.getId(),
                car.getBody().getId(), car.getMotor().getId(), car.getAccessory().getId());

        LOGGER.info(logLine);

        try (FileWriter fw = new FileWriter("factory_log.txt", true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logLine);
            bw.newLine();
        } catch (IOException e) {
            LOGGER.warning("Ошибка записи лога: " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        Thread.currentThread().interrupt();
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