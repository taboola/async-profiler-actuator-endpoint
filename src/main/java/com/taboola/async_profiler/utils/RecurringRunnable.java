package com.taboola.async_profiler.utils;

public class RecurringRunnable implements Runnable {

    private final Runnable baseRunnable;
    private volatile boolean isCancelled;

    public RecurringRunnable(Runnable baseRunnable) {
        this.baseRunnable = baseRunnable;
        this.isCancelled = false;
    }

    @Override
    public void run() {
        while (!isCancelled) {
            baseRunnable.run();
        }
    }

    public void cancel() {
        isCancelled = true;
    }
}
