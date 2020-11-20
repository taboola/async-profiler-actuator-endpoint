package com.taboola.async_profiler.api.facade.profiler;

import com.taboola.async_profiler.api.original.Counter;

public interface AsyncProfilerInterface {
    void start(String event, long interval) throws IllegalStateException;
    void stop() throws IllegalStateException;
    String execute(String command) throws IllegalArgumentException, java.io.IOException;
    String dumpCollapsed(Counter counter);
    void addThread(Thread thread);
}