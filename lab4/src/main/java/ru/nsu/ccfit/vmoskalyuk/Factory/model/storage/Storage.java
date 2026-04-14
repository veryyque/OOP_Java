package ru.nsu.ccfit.vmoskalyuk.Factory.model.storage;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.observable.Observable;
import java.util.LinkedList;

public class Storage<T> extends Observable {

    private final LinkedList<T> items = new LinkedList<>();
    private final int maxSize;
    private final String name;

    private int totalProduced = 0;
    private int totalConsumed = 0;

    public Storage(int maxSize, String name) {
        this.maxSize = maxSize;
        this.name = name;
    }

    public void put(T item) {
        if (item == null) return;

        synchronized (items) {
            try {
                while (items.size() >= maxSize) {
                    if (Thread.currentThread().isInterrupted()) return;
                    items.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            items.addFirst(item);
            totalProduced++;
            items.notifyAll();
        }

        setChanged();
        notifyObservers("put");
    }
    public T take() throws InterruptedException {
        synchronized (items) {
            try {
                while (items.isEmpty()) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    items.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;                       // пробрасываем дальше
            }

            T item = items.removeLast();
            totalConsumed++;
            items.notifyAll();
            return item;
        }
    }

    public int size() {
        synchronized (items) {
            return items.size();
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getTotalProduced() { return totalProduced; }
    public int getTotalConsumed() { return totalConsumed; }
    public String getName() { return name; }
}