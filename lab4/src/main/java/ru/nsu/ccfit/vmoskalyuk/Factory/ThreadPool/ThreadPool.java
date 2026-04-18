package ru.nsu.ccfit.vmoskalyuk.Factory.ThreadPool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool {
    private final Queue<Runnable> taskQueue = new LinkedList<>();
    private final Thread[] workers;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ThreadPool(int poolSize) {
        workers = new Thread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(taskQueue, running);
            workers[i] = new Thread(worker, "Assembler-" + i);
            workers[i].start();
        }
    }

    public void submit(Runnable task) {
        if (!running.get() || task == null) {
            return;
        }
        synchronized (taskQueue) {
            taskQueue.add(task);
            taskQueue.notifyAll();
        }
    }

    public int getQueueSize() {
        synchronized (taskQueue) {
            return taskQueue.size();
        }
    }

    public void shutdown() {
        running.set(false);
        for (Thread worker : workers) worker.interrupt();
        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }
        for (Thread worker : workers) {
            try {
                worker.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}