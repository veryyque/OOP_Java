package ru.nsu.ccfit.vmoskalyuk.Factory.model;

import ru.nsu.ccfit.vmoskalyuk.Factory.config.Config;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.*;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observable;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observer;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers.*;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.dealers.Dealer;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.controller.StorageController;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.workers.AssemblyTask;
import ru.nsu.ccfit.vmoskalyuk.Factory.ThreadPool.ThreadPool;


import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CarFactory extends Observable implements Observer {

    private static final DateTimeFormatter LOG_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

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
    private final boolean saleLoggingEnabled;

    private final PrintWriter logWriter;   // ЕДИНЫЙ лог для всего

    private final List<ModelListener> listeners = new ArrayList<>();

    public interface ModelListener {
        void onModelChanged();
    }

    public CarFactory(Config config) {
        this.config = config;
        this.saleLoggingEnabled = config.getBoolean("log.sale");

        // Создаём единый лог-файл
        this.logWriter = createLogWriter();

        // Склады
        bodyStorage = new Storage<>(config.getInt("storage.body.size"), "BodyStorage", logWriter);
        motorStorage = new Storage<>(config.getInt("storage.motor.size"), "MotorStorage", logWriter);
        accessoryStorage = new Storage<>(config.getInt("storage.accessory.size"), "AccessoryStorage", logWriter);
        carStorage = new Storage<>(config.getInt("storage.car.size"), "CarStorage", logWriter);

        // Поставщики (с передачей logWriter)
        bodySupplier = new BodySupplier(bodyStorage, config.getInt("delay.body"), logWriter);
        motorSupplier = new MotorSupplier(motorStorage, config.getInt("delay.motor"), logWriter);

        int accCount = config.getInt("suppliers.accessory.count");
        for (int i = 0; i < accCount; i++) {
            accessorySuppliers.add(new AccessorySupplier(accessoryStorage,
                    config.getInt("delay.accessory"), i + 1, logWriter));
        }

        threadPool = new ThreadPool(config.getInt("workers.count"));
        storageController = new StorageController(carStorage, this::requestAssembly, threadPool::getQueueSize);

        // Дилеры
        int dealersCount = config.getInt("dealers.count");
        for (int i = 1; i <= dealersCount; i++) {
            Dealer dealer = new Dealer(carStorage, i, this, config.getInt("delay.dealer"), logWriter, saleLoggingEnabled);
            dealers.add(dealer);
            dealerThreads.add(new Thread(dealer, "Dealer-" + i));
        }

        log("Фабрика инициализирована");
    }

    /** Создаёт PrintWriter для factory.log */
    private PrintWriter createLogWriter() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("factory.log", true), true);
            writer.println("=== ФАБРИКА ЗАПУЩЕНА " + LocalDateTime.now().format(LOG_FORMATTER) + " ===");
            return writer;
        } catch (IOException e) {
            System.err.println("Не удалось создать лог-файл: " + e.getMessage());
            return new PrintWriter(System.out); // fallback в консоль
        }
    }

    /** Единый метод логирования — используется всей фабрикой */
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
        String fullLine = "[" + timestamp + "] " + message;

        synchronized (logWriter) {
            logWriter.println(fullLine);
        }
        System.out.println(fullLine); // дублируем в консоль
    }

    private void requestAssembly() {
        AssemblyTask task = new AssemblyTask(
                bodyStorage, motorStorage, accessoryStorage, carStorage, this, logWriter);
        threadPool.submit(task);
    }

    public void onCarAssembled(Car car) {
        totalCarsProduced.incrementAndGet();
        notifyListeners();
    }

    private void onCarSold(int dealerNumber, Car car) {
        notifyListeners();
    }

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
        log("[GUI] Скорость кузовов → " + delay + " мс");
    }

    public void setMotorDelay(int delay) {
        motorSupplier.setDelay(delay);
        log("[GUI] Скорость двигателей → " + delay + " мс");
    }

    public void setAccessoryDelay(int delay) {
        accessorySuppliers.forEach(s -> s.setDelay(delay));
        log("[GUI] Скорость аксессуаров → " + delay + " мс");
    }

    public void setDealerDelay(int delay) {
        dealers.forEach(d -> d.setDelay(delay));
        log("[GUI] Скорость дилеров → " + delay + " мс");
    }

    // ====================== Геттеры для GUI ======================
    public int getBodyCount() { return bodyStorage.size(); }
    public int getMotorCount() { return motorStorage.size(); }
    public int getAccessoryCount() { return accessoryStorage.size(); }
    public int getCarCount() { return carStorage.size(); }
    public int getTotalCarsProduced() { return totalCarsProduced.get(); }
    public int getPendingTasks() { return threadPool.getQueueSize(); }
    public int getBodyCapacity() {
        return bodyStorage.getMaxSize();
    }

    public int getMotorCapacity() {
        return motorStorage.getMaxSize();
    }

    public int getAccessoryCapacity() {
        return accessoryStorage.getMaxSize();
    }

    public int getCarCapacity() {
        return carStorage.getMaxSize();
    }
    public int getTotalBodiesProduced() { return bodyStorage.getTotalProduced(); }
    public int getTotalMotorsProduced() { return motorStorage.getTotalProduced(); }
    public int getTotalAccessoriesProduced() { return accessoryStorage.getTotalProduced(); }
    public int getInitialBodyDelay() { return config.getInt("delay.body"); }
    public int getInitialMotorDelay() { return config.getInt("delay.motor"); }
    public int getInitialAccessoryDelay() { return config.getInt("delay.accessory"); }
    public int getInitialDealerDelay() { return config.getInt("delay.dealer"); }

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

        log("ФАБРИКА УСПЕШНО ЗАПУЩЕНА");
    }

    public void shutdown() {
        log("Остановка фабрики...");
        bodySupplier.shutdown();
        motorSupplier.shutdown();
        accessorySuppliers.forEach(AccessorySupplier::shutdown);
        dealers.forEach(Dealer::shutdown);

        supplierThreads.forEach(Thread::interrupt);
        dealerThreads.forEach(Thread::interrupt);

        threadPool.shutdown();
        storageController.shutdown();

        log("=== ФАБРИКА ОСТАНОВЛЕНА ===");
        if (logWriter != null) {
            logWriter.close();
        }
    }
}