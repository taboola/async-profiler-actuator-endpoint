package com.taboola.async_profiler.api.facade.profiler;

public interface AsyncProfiler {
    String execute(String command) throws IllegalArgumentException, java.io.IOException;
    void addThread(Thread thread);
}