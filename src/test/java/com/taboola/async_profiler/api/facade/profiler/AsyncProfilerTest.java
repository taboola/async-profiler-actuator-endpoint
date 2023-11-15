package com.taboola.async_profiler.api.facade.profiler;

import io.pyroscope.one.profiler.Counter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AsyncProfilerTest {

    @Mock
    private io.pyroscope.one.profiler.AsyncProfiler delegate;

    @InjectMocks
    private AsyncProfiler asyncProfiler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testStart() {
        String event = "event";
        long interval = 0L;
        asyncProfiler.start(event, interval);
        verify(delegate, times(1)).start(eq(event), eq(interval));
    }

    @Test
    public void testResume() {
        String event = "event";
        long interval = 0L;
        asyncProfiler.resume(event, interval);
        verify(delegate, times(1)).resume(eq(event), eq(interval));
    }

    @Test
    public void testStop() {
        asyncProfiler.stop();
        verify(delegate, times(1)).stop();
    }

    @Test
    public void testGetSamples() {
        long samples = 1L;
        when(delegate.getSamples()).thenReturn(samples);
        assertEquals(samples, asyncProfiler.getSamples());
        verify(delegate, times(1)).getSamples();
    }

    @Test
    public void testGetVersion() {
        String version = "1";
        when(delegate.getVersion()).thenReturn(version);
        assertEquals(version, asyncProfiler.getVersion());
        verify(delegate, times(1)).getVersion();
    }

    @Test
    public void testExecute() throws IOException {
        String command = "command";
        String result = "result";
        when(delegate.execute(eq(command))).thenReturn(result);
        assertEquals(result, asyncProfiler.execute(command));
        verify(delegate, times(1)).execute(eq(command));
    }

    @Test
    public void testDumpCollapsed() {
        Counter counter = Counter.SAMPLES;
        String output = "output";
        when(delegate.dumpCollapsed(eq(counter))).thenReturn(output );
        assertEquals(output, asyncProfiler.dumpCollapsed(counter));
        verify(delegate, times(1)).dumpCollapsed(eq(counter));
    }

    @Test
    public void testDumpTraces() {
        int maxTraces = 1;
        String output = "output";
        when(delegate.dumpTraces(eq(maxTraces))).thenReturn(output);
        assertEquals(output, asyncProfiler.dumpTraces(maxTraces));
        verify(delegate, times(1)).dumpTraces(eq(maxTraces));
    }

    @Test
    public void testDumpFlat() {
        int maxMethods = 1;
        String output = "output";
        when(delegate.dumpFlat(eq(maxMethods))).thenReturn(output);
        assertEquals(output, asyncProfiler.dumpFlat(maxMethods));
        verify(delegate, times(1)).dumpFlat(eq(maxMethods));
    }

    @Test
    public void testAddThread() {
        Thread thread = mock(Thread.class);
        asyncProfiler.addThread(thread);
        verify(delegate, times(1)).addThread(eq(thread));
    }
}