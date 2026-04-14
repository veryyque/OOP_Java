// ru/nsu/ccfit/vmoskalyuk/Factory/model/details/Detail.java
package ru.nsu.ccfit.vmoskalyuk.Factory.model.details;

public abstract class Detail {
    private static long idCounter = 0;

    private final long id;
    private volatile boolean used = false;

    protected Detail() {
        synchronized (Detail.class) {
            this.id = ++idCounter;
        }
    }

    public long getId() {
        return id;
    }

    public boolean isUsed() {
        return used;
    }

    public void markAsUsed() {
        if (used) {
            throw new IllegalStateException("Деталь с ID=" + id + " уже использована в другой машине!");
        }
        this.used = true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + id + (used ? " [USED]" : "");
    }
}