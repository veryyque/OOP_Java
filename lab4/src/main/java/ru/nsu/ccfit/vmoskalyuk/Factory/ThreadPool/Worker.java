// Worker.java - отдельный файл
package ru.nsu.ccfit.vmoskalyuk.Factory.ThreadPool;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker implements Runnable {
    private final Queue<Runnable> taskQueue;
    private final AtomicBoolean running;

    public Worker(Queue<Runnable> taskQueue, AtomicBoolean running) {
        this.taskQueue = taskQueue;
        this.running = running;
    }

    @Override
    public void run() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            Runnable task;
            synchronized (taskQueue) {
                while (taskQueue.isEmpty() && running.get()) {
                    try { taskQueue.wait(); }
                    catch (InterruptedException e) { return; }
                }
                if (!running.get()) return;
                task = taskQueue.poll();
            }
            if (task != null) task.run();
        }
    }
}