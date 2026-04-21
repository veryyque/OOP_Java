package ru.nsu.ccfit.vmoskalyuk.Factory.model.storage;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class Storage<T> {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final LinkedList<T> items = new LinkedList<>();
    private final int maxSize;
    private final String name;
    private final PrintWriter logWriter;

    private int totalProduced = 0;
    private int totalConsumed = 0;

    public Storage(int maxSize, String name, PrintWriter logWriter) {
        this.maxSize = maxSize;
        this.name = name;
        this.logWriter = logWriter;
        log("Склад создан (вместимость = " + maxSize + ")");
    }

    private void log(String message) {
        String ts = LocalDateTime.now().format(TIME_FORMAT);
        synchronized (logWriter) {
            logWriter.println("[" + ts + "] [" + name + "] " + message);
        }
    }

    public void put(T item) throws InterruptedException {
        if (item == null) return;

        synchronized (this) {
            while (items.size() >= maxSize) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                wait();
            }

            items.addFirst(item);
            totalProduced++;

            log("+ PUT " + item);
            notifyAll();
        }
    }

    public T take() throws InterruptedException {
        synchronized (this) {
            while (items.isEmpty()) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                wait();
            }

            T item = items.removeLast();
            totalConsumed++;

            log("- TAKE " + item);
            notifyAll();
            return item;
        }
    }

    public int size() {
        synchronized (this) { return items.size(); }
    }

    public int getMaxSize() { return maxSize; }
    public int getTotalProduced() { synchronized (this) { return totalProduced; } }
}