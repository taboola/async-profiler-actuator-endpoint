package com.taboola.async_profiler.api.facade.profiler;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Lazy AsyncProfiler implementation to load the profiler itself only on the first actual usage, using the provided loader function.
 * */
public class LazyLoadedAsyncProfiler implements AsyncProfiler {

	private final Supplier<AsyncProfiler> asyncProfilerLoader;
	private volatile AsyncProfiler asyncProfiler;

	public LazyLoadedAsyncProfiler(Supplier<AsyncProfiler> asyncProfilerLoader) {
		this.asyncProfilerLoader = asyncProfilerLoader;
	}

	@Override
	public String execute(String command) throws IllegalArgumentException, IOException {
		return getOrLoadAsyncProfiler().execute(command);
	}

	@Override
	public void addThread(Thread thread) {
		getOrLoadAsyncProfiler().addThread(thread);
	}

	AsyncProfiler getOrLoadAsyncProfiler() {
		AsyncProfiler result = this.asyncProfiler;

		if (result == null) {
			synchronized (this) {
				result = asyncProfiler;
				if (result == null) {
					asyncProfiler = result = asyncProfilerLoader.get();
				}
			}
		}

		return result;
	}
}
