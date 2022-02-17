package com.taboola.async_profiler.api.facade.profiler;

import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;

public class EmptyAsyncProfilerTest {

    private EmptyAsyncProfiler emptyAsyncProfiler;

    @Before
    public void setup() {
        emptyAsyncProfiler = new EmptyAsyncProfiler(new Throwable());
    }

    @Test
    public void testExecuteShouldThrow() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.execute("start"));
    }

    @Test
    public void testAddThreadShouldThrow() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.addThread(Thread.currentThread()));
    }
}