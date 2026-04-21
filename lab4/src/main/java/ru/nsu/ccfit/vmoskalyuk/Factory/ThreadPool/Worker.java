// Worker.java - отдельный файл
package ru.nsu.ccfit.vmoskalyuk.Factory.ThreadPool;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker implements Runnable {
    private final Queue<Runnable> taskQueue;

    public Worker(Queue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Runnable task;
            synchronized (taskQueue) {
                while (taskQueue.isEmpty()) {
                    try { taskQueue.wait(); }
                    catch (InterruptedException e) { return; }
                }
                task = taskQueue.poll();
            }
            if (task != null) task.run();
        }
    }
}