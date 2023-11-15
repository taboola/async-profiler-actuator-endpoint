package com.taboola.async_profiler.api.facade.profiler;

import io.pyroscope.one.profiler.AsyncProfilerMXBean;
import io.pyroscope.one.profiler.Counter;
import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor
public class AsyncProfiler implements AsyncProfilerMXBean {

    private io.pyroscope.one.profiler.AsyncProfiler delegate;

    public AsyncProfiler(io.pyroscope.one.profiler.AsyncProfiler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void start(String event, long interval) throws IllegalStateException {
        delegate.start(event, interval);
    }

    @Override
    public void resume(String event, long interval) throws IllegalStateException {
        delegate.resume(event, interval);
    }

    @Override
    public void stop() throws IllegalStateException {
        delegate.stop();
    }

    @Override
    public long getSamples() {
        return delegate.getSamples();
    }

    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    @Override
    public String execute(String command) throws IOException {
        return delegate.execute(command);
    };

    @Override
    public String dumpCollapsed(Counter counter) {
        return delegate.dumpCollapsed(counter);
    }

    @Override
    public String dumpTraces(int maxTraces) {
        return delegate.dumpTraces(maxTraces);
    }

    @Override
    public String dumpFlat(int maxMethods) {
        return delegate.dumpFlat(maxMethods);
    }

    public void addThread(Thread thread) {
        delegate.addThread(thread);
    }
}