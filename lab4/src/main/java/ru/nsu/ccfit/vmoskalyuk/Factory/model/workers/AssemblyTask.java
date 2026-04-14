// ru/nsu/ccfit/vmoskalyuk/Factory/model/workers/AssemblyTask.java
package ru.nsu.ccfit.vmoskalyuk.Factory.model.workers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.*;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.CarFactory;

public class AssemblyTask implements Runnable {

    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;
    private final CarFactory carFactory;

    public AssemblyTask(Storage<Body> bodyStorage,
                        Storage<Motor> motorStorage,
                        Storage<Accessory> accessoryStorage,
                        Storage<Car> carStorage,
                        CarFactory carFactory) {
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
        this.carFactory = carFactory;
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
            carStorage.put(car);

            carFactory.onCarAssembled();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка при сборке машины: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}