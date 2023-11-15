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
    public void testAddThread() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.addThread(Thread.currentThread()));
    }

    @Test
    public void testStart() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.start("itimer", 0));
    }

    @Test
    public void testResume() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.resume("itimer", 0));
    }

    @Test
    public void testStop() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.stop());
    }

    @Test
    public void testGetSamples() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.resume("itimer", 0));

    }

    @Test
    public void testGetVersion() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.getVersion());
    }

    @Test
    public void testExecute() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.execute("com"));
    }

    @Test
    public void testDumpCollapsed() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.dumpCollapsed(null));
    }

    @Test
    public void testTraces() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.dumpTraces(1));
    }

    @Test
    public void testDumpFlat() {
        assertThrows(IllegalStateException.class, () -> emptyAsyncProfiler.dumpFlat(1));
    }

}