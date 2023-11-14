package com.taboola.async_profiler.api.facade.profiler;

import io.pyroscope.one.profiler.Counter;

import java.io.IOException;

/**
 * Dummy profiler implementation which is used when we fail to load the native lib.
 * */
public class EmptyAsyncProfiler extends AsyncProfiler {

    private Throwable cause;

    public EmptyAsyncProfiler(Throwable cause) {
        this.cause = cause;
    }

    private static final String ERROR_MESSAGE = "Failed loading async profiler lib: ";
    @Override
    public void start(String s, long l) throws IllegalStateException {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }

    @Override
    public void resume(String s, long l) throws IllegalStateException {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }

    @Override
    public void stop() throws IllegalStateException {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }

    @Override
    public long getSamples() {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }

    @Override
    public String getVersion() {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }

    @Override
    public String execute(String command) throws IllegalArgumentException, IOException {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }

    @Override
    public String dumpCollapsed(Counter counter) {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }

    @Override
    public String dumpTraces(int i) {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }

    @Override
    public String dumpFlat(int i) {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }

    @Override
    public void addThread(Thread thread) {
        throw new IllegalStateException(ERROR_MESSAGE + cause.getMessage(), cause);
    }
}
