package com.taboola.async_profiler.api.facade.profiler;

import java.io.IOException;

/**
 * Dummy profiler implementation which is used when we fail to load the native lib.
 * */
public class EmptyAsyncProfiler implements AsyncProfiler {

    private static final String ERROR_MESSAGE = "Failed loading async profiler lib";

    @Override
    public String execute(String command) throws IllegalArgumentException, IOException {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    @Override
    public void addThread(Thread thread) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }
}
