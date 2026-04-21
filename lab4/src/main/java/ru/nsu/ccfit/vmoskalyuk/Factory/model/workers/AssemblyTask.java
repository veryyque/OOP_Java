// ru/nsu/ccfit/vmoskalyuk/Factory/model/workers/AssemblyTask.java
package ru.nsu.ccfit.vmoskalyuk.Factory.model.workers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.*;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.CarFactory;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssemblyTask implements Runnable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;
    private final CarFactory carFactory;
    private final PrintWriter logWriter;

    public AssemblyTask(Storage<Body> bodyStorage, Storage<Motor> motorStorage, Storage<Accessory> accessoryStorage, Storage<Car> carStorage, CarFactory carFactory, PrintWriter logWriter) {
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
        this.carFactory = carFactory;
        this.logWriter = logWriter;
    }

    @Override
    public void run() {
        try {
            Body body = bodyStorage.take();
            Motor motor = motorStorage.take();
            Accessory accessory = accessoryStorage.take();

            if (body == null || motor == null || accessory == null) {
                return;
            }

            Car car = new Car(body, motor, accessory);

            logAssembly(car);

            carStorage.put(car);

            carFactory.onCarAssembled(car);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Ошибка при сборке машины: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void logAssembly(Car car) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String workerName = Thread.currentThread().getName();
        String logLine = String.format("%s [AssemblyTask] Worker %s ASSEMBLED Car #%d (Body:%d, Motor:%d, Accessory:%d)",
                timestamp, workerName, car.getId(),
                car.getBody().getId(), car.getMotor().getId(), car.getAccessory().getId());

        synchronized (logWriter) {
            logWriter.println(logLine);
            logWriter.flush();
        }
    }
}