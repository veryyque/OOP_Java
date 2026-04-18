package ru.nsu.ccfit.vmoskalyuk.Factory.model.observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Простая и безопасная реализация Observable/Observer.
 * Не использует setChanged() + notifyObservers(), чтобы избежать deadlock'ов со Storage.
 */
public class Observable {

    private final List<Observer> observers = new ArrayList<>();

    /**
     * Добавляет наблюдателя
     */
    public void addObserver(Observer observer) {
        if (observer == null) return;
        synchronized (observers) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    /**
     * Удаляет наблюдателя
     */
    public void deleteObserver(Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    /**
     * Уведомляет всех наблюдателей с аргументом.
     * Вызывать можно из любого synchronized блока — безопасно.
     */
    public void notifyObservers(Object arg) {
        List<Observer> copy;

        synchronized (observers) {
            copy = new ArrayList<>(observers);   // защита от ConcurrentModification
        }

        // Важно: уведомления вызываются БЕЗ удержания локов!
        for (Observer observer : copy) {
            try {
                observer.update(this, arg);
            } catch (Exception e) {
                // логируем, но не падаем
                e.printStackTrace();
            }
        }
    }

    /**
     * Уведомляет без аргумента
     */
    public void notifyObservers() {
        notifyObservers(null);
    }
}