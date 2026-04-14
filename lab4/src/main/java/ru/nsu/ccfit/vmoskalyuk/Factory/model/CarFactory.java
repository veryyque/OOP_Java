// ru/nsu/ccfit/vmoskalyuk/Factory/model/CarFactory.java
package ru.nsu.ccfit.vmoskalyuk.Factory.model;

import ru.nsu.ccfit.vmoskalyuk.Factory.config.Config;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.*;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers.*;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.dealers.Dealer;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.controller.StorageController;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.workers.AssemblyTask;
import ru.nsu.ccfit.vmoskalyuk.Factory.ThreadPool.ThreadPool;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observable;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observer;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CarFactory extends Observable implements Observer {

    private final Config config;

    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;

    private final BodySupplier bodySupplier;
    private final MotorSupplier motorSupplier;
    private final List<AccessorySupplier> accessorySuppliers = new ArrayList<>();

    private final List<Thread> supplierThreads = new ArrayList<>();
    private final List<Dealer> dealers = new ArrayList<>();
    private final List<Thread> dealerThreads = new ArrayList<>();

    private final ThreadPool threadPool;

    private final StorageController storageController;

    private final AtomicInteger totalCarsProduced = new AtomicInteger(0);

    private PrintWriter logWriter = null;

    private final List<ModelListener> listeners = new ArrayList<>();

    public interface ModelListener {
        void onModelChanged();
    }

    public CarFactory(Config config) {
        this.config = config;

        bodyStorage = new Storage<>(config.getInt("storage.body.size"), "BodyStorage");
        motorStorage = new Storage<>(config.getInt("storage.motor.size"), "MotorStorage");
        accessoryStorage = new Storage<>(config.getInt("storage.accessory.size"), "AccessoryStorage");
        carStorage = new Storage<>(config.getInt("storage.car.size"), "CarStorage");

        bodySupplier = new BodySupplier(bodyStorage, config.getInt("delay.body"));
        motorSupplier = new MotorSupplier(motorStorage, config.getInt("delay.motor"));

        int accCount = config.getInt("suppliers.accessory.count");
        for (int i = 0; i < accCount; i++) {
            accessorySuppliers.add(new AccessorySupplier(accessoryStorage, config.getInt("delay.accessory")));
        }

        threadPool = new ThreadPool(config.getInt("workers.count"));

        storageController = new StorageController(carStorage, this::requestAssembly);

        int dealersCount = config.getInt("dealers.count");
        for (int i = 1; i <= dealersCount; i++) {
            Dealer dealer = new Dealer(
                    carStorage,
                    i,
                    this,                                      // CarFactory как Observer
                    config.getInt("delay.dealer")
            );
            dealers.add(dealer);
            dealerThreads.add(new Thread(dealer, "Dealer-" + i));
        }

        if (config.getBoolean("log.sale")) {
            try {
                logWriter = new PrintWriter(new FileWriter("factory_log.txt", true));
            } catch (IOException e) {
                System.err.println("Не удалось открыть лог-файл: " + e.getMessage());
            }
        }
    }

    // ====================== Запрос на сборку ======================
    private void requestAssembly() {
        AssemblyTask task = new AssemblyTask(
                bodyStorage,
                motorStorage,
                accessoryStorage,
                carStorage,
                this                    // передаём CarFactory
        );
        threadPool.submit(task);
    }

    // ====================== Вызывается из AssemblyTask ======================
    public void onCarAssembled() {
        totalCarsProduced.incrementAndGet();
        notifyListeners();
    }

    // ====================== Обработка продажи от Dealer ======================
    private void onCarSold(int dealerNumber, Car car) {
        if (logWriter != null) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            logWriter.printf("%s: Dealer %d: Auto %d (Body: %d, Motor: %d, Accessory: %d)%n",
                    time, dealerNumber, car.getId(),
                    car.getBody().getId(), car.getMotor().getId(), car.getAccessory().getId());
            logWriter.flush();
        }
        notifyListeners();
    }

    // ====================== Реализация Observer ======================
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Dealer.SaleEvent) {
            Dealer.SaleEvent event = (Dealer.SaleEvent) arg;
            onCarSold(event.dealerNumber, event.car);
        }
    }

    // ====================== Управление скоростью ======================
    public void setBodyDelay(int delay) {
        bodySupplier.setDelay(delay);
    }

    public void setMotorDelay(int delay) {
        motorSupplier.setDelay(delay);
    }

    public void setAccessoryDelay(int delay) {
        accessorySuppliers.forEach(s -> s.setDelay(delay));
    }

    public void setDealerDelay(int delay) {
        dealers.forEach(d -> d.setDelay(delay));
    }

    // ====================== Геттеры для View ======================
    public int getBodyCount() { return bodyStorage.size(); }
    public int getMotorCount() { return motorStorage.size(); }
    public int getAccessoryCount() { return accessoryStorage.size(); }
    public int getCarCount() { return carStorage.size(); }
    public int getTotalCarsProduced() { return totalCarsProduced.get(); }

    public int getTotalBodiesProduced() {
        return bodyStorage.getTotalProduced();
    }

    public int getTotalMotorsProduced() {
        return motorStorage.getTotalProduced();
    }

    public int getTotalAccessoriesProduced() {
        return accessoryStorage.getTotalProduced();
    }
    public int getBodyCapacity() { return bodyStorage.getMaxSize(); }
    public int getMotorCapacity() { return motorStorage.getMaxSize(); }
    public int getAccessoryCapacity() { return accessoryStorage.getMaxSize(); }
    public int getCarCapacity() { return carStorage.getMaxSize(); }
    public int getPendingTasks() { return threadPool.getQueueSize(); }

    // ====================== Слушатели View ======================
    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        listeners.forEach(ModelListener::onModelChanged);
    }

    // ====================== Запуск и остановка ======================
    public void start() {
        supplierThreads.add(new Thread(bodySupplier, "BodySupplier"));
        supplierThreads.add(new Thread(motorSupplier, "MotorSupplier"));
        for (int i = 0; i < accessorySuppliers.size(); i++) {
            supplierThreads.add(new Thread(accessorySuppliers.get(i), "AccessorySupplier-" + (i + 1)));
        }

        supplierThreads.forEach(Thread::start);
        dealerThreads.forEach(Thread::start);

        storageController.start();

        System.out.println("Фабрика успешно запущена");
    }

    public void shutdown() {
        bodySupplier.shutdown();
        motorSupplier.shutdown();
        accessorySuppliers.forEach(AccessorySupplier::shutdown);
        dealers.forEach(Dealer::shutdown);

        supplierThreads.forEach(Thread::interrupt);
        dealerThreads.forEach(Thread::interrupt);

        threadPool.shutdown();
        storageController.shutdown();

        if (logWriter != null) {
            logWriter.close();
        }

        System.out.println("Фабрика остановлена");
    }
}