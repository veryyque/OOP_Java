package ru.nsu.ccfit.vmoskalyuk.Factory.ThreadPool;

import java.util.LinkedList;
import java.util.Queue;

public class ThreadPool {
    private final Queue<Runnable> taskQueue = new LinkedList<>();
    private final Thread[] workers;
    private volatile boolean running = true;

    public ThreadPool(int poolSize) {
        workers = new Thread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(taskQueue, running);
            workers[i] = new Thread(worker, "Assembler-" + i);
            workers[i].start();
        }
    }

    public void submit(Runnable task) {
        synchronized (taskQueue) {
            taskQueue.add(task);
            taskQueue.notify();
        }
    }

    public int getQueueSize() {
        synchronized (taskQueue) {
            return taskQueue.size();
        }
    }

    public void shutdown() {
        running = false;
        for (Thread worker : workers) worker.interrupt();
        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }
    }
}