// ru/nsu/ccfit/vmoskalyuk/Factory/ThreadPool/Task.java
package ru.nsu.ccfit.vmoskalyuk.Factory.ThreadPool;

public abstract class Task implements Runnable {

    protected volatile boolean interrupted = false;

    public void interrupt() {
        interrupted = true;
    }

    protected boolean isInterrupted() {
        return interrupted || Thread.currentThread().isInterrupted();
    }
}