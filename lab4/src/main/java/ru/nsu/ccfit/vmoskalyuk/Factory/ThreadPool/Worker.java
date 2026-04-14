// Worker.java - отдельный файл
package ru.nsu.ccfit.vmoskalyuk.Factory.ThreadPool;

import java.util.Queue;

public class Worker implements Runnable {
    private final Queue<Runnable> taskQueue;
    private volatile boolean running;

    public Worker(Queue<Runnable> taskQueue, boolean running) {
        this.taskQueue = taskQueue;
        this.running = running;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            Runnable task;
            synchronized (taskQueue) {
                while (taskQueue.isEmpty() && running) {
                    try { taskQueue.wait(); }
                    catch (InterruptedException e) { return; }
                }
                if (!running) return;
                task = taskQueue.poll();
            }
            if (task != null) task.run();
        }
    }
}