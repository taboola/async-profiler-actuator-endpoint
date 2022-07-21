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

import org.junit.Test;

public class LazyLoadedAsyncProfilerTest {

	@Test
	public void testProfilerIsLazyLoadedOnce() throws IOException {
		Supplier<AsyncProfiler> asyncProfilerLoader = mock(Supplier.class);
		when(asyncProfilerLoader.get()).thenReturn(mock(AsyncProfiler.class));
		LazyLoadedAsyncProfiler lazyLoadedAsyncProfiler = new LazyLoadedAsyncProfiler(asyncProfilerLoader);
		verifyZeroInteractions(asyncProfilerLoader);

		lazyLoadedAsyncProfiler.execute("some command");
		lazyLoadedAsyncProfiler.execute("some command2");
		lazyLoadedAsyncProfiler.addThread(mock(Thread.class));
		verify(asyncProfilerLoader, times(1)).get();
	}

	@Test
	public void testExecute() throws IOException {
		Supplier<AsyncProfiler> asyncProfilerLoader = mock(Supplier.class);
		AsyncProfiler asyncProfiler = mock(AsyncProfiler.class);

		when(asyncProfilerLoader.get()).thenReturn(asyncProfiler);
		when(asyncProfiler.execute(eq("some command"))).thenReturn("result");

		LazyLoadedAsyncProfiler lazyLoadedAsyncProfiler = new LazyLoadedAsyncProfiler(asyncProfilerLoader);
		String result = lazyLoadedAsyncProfiler.execute("some command");

		verify(asyncProfilerLoader, times(1)).get();
		assertEquals("result", result);
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