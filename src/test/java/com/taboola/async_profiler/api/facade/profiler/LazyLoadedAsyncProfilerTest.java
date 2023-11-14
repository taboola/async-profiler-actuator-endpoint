package com.taboola.async_profiler.api.facade.profiler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.function.Supplier;

import io.pyroscope.one.profiler.Counter;
import org.junit.Before;
import org.junit.Test;

public class LazyLoadedAsyncProfilerTest {
	private Supplier<AsyncProfiler> asyncProfilerLoader;
	private AsyncProfiler asyncProfiler;
	private LazyLoadedAsyncProfiler lazyLoadedAsyncProfiler;

	@Before
	public void setup() {
		asyncProfilerLoader = mock(Supplier.class);
		asyncProfiler = mock(AsyncProfiler.class);
		when(asyncProfilerLoader.get()).thenReturn(asyncProfiler);
		lazyLoadedAsyncProfiler = new LazyLoadedAsyncProfiler(asyncProfilerLoader);
	}

	@Test
	public void testProfilerIsLazyLoadedOnce() throws IOException {
		LazyLoadedAsyncProfiler lazyLoadedAsyncProfiler = new LazyLoadedAsyncProfiler(asyncProfilerLoader);
		verifyZeroInteractions(asyncProfilerLoader);

		lazyLoadedAsyncProfiler.execute("some command");
		lazyLoadedAsyncProfiler.execute("some command2");
		lazyLoadedAsyncProfiler.addThread(mock(Thread.class));
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testStart() {
		lazyLoadedAsyncProfiler.start("event", 0);
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testResume() {
		lazyLoadedAsyncProfiler.resume("event", 0);
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testStop() {
		lazyLoadedAsyncProfiler.stop();
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testGetSamples() {
		long samples = 1;
		when(asyncProfiler.getSamples()).thenReturn(samples);
		assertEquals(samples, lazyLoadedAsyncProfiler.getSamples());
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testGetVersion() throws IOException {
		String version = "1";
		when(asyncProfiler.getVersion()).thenReturn(version);
		assertEquals(version, lazyLoadedAsyncProfiler.getVersion());
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testExecute() throws IOException {
		String command = "command";
		String result = "result";
		when(asyncProfiler.execute(eq(command))).thenReturn(result);

		assertEquals(result, lazyLoadedAsyncProfiler.execute(command));
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testDumpCollapsed() {
		Counter counter = Counter.SAMPLES;
		String output = "output";
		when(asyncProfiler.dumpCollapsed(eq(counter))).thenReturn(output);

		assertEquals(output, lazyLoadedAsyncProfiler.dumpCollapsed(counter));
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testDumpTraces() {
		int maxTraces = 1;
		String output = "output";
		when(asyncProfiler.dumpTraces(eq(maxTraces))).thenReturn(output);

		assertEquals(output, lazyLoadedAsyncProfiler.dumpTraces(maxTraces));
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testDumpFlat() {
		int maxMethods = 1;
		String output = "output";
		when(asyncProfiler.dumpFlat(eq(maxMethods))).thenReturn(output);

		assertEquals(output, lazyLoadedAsyncProfiler.dumpFlat(maxMethods));
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testAddThread() {
		Supplier<AsyncProfiler> asyncProfilerLoader = mock(Supplier.class);
		AsyncProfiler asyncProfiler = mock(AsyncProfiler.class);
		Thread thread = mock(Thread.class);

		when(asyncProfilerLoader.get()).thenReturn(asyncProfiler);

		LazyLoadedAsyncProfiler lazyLoadedAsyncProfiler = new LazyLoadedAsyncProfiler(asyncProfilerLoader);
		lazyLoadedAsyncProfiler.addThread(thread);

		verify(asyncProfilerLoader, times(1)).get();
		verify(asyncProfiler, times(1)).addThread(same(thread));
	}
}