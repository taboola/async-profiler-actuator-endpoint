package com.taboola.async_profiler.api.facade.profiler;

import java.io.IOException;

import com.taboola.async_profiler.api.original.Counter;

/**
 * Dummy profiler implementation which is used when we fail to load the native lib.
 * */
public class EmptyAsyncProfiler implements AsyncProfiler {

    private static final String ERROR_MESSAGE = "Failed loading async profiler lib";

    @Override
    public void start(String event, long interval) throws IllegalStateException {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    @Override
    public void stop() throws IllegalStateException {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    @Override
    public String execute(String command) throws IllegalArgumentException, IOException {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    @Override
    public String dumpCollapsed(Counter counter) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    @Override
    public void addThread(Thread thread) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }
}
