package com.taboola.async_profiler.api.facade.profiler;

import io.pyroscope.one.profiler.Counter;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Lazy AsyncProfiler implementation to load the profiler itself only on the first actual usage, using the provided loader function.
 * */
public class LazyLoadedAsyncProfiler extends AsyncProfiler {

	private final Supplier<AsyncProfiler> asyncProfilerLoader;
	private volatile AsyncProfiler asyncProfiler;

	public LazyLoadedAsyncProfiler(Supplier<AsyncProfiler> asyncProfilierLoader) {
		this.asyncProfilerLoader = asyncProfilierLoader;
	}

	@Override
	public void start(String event, long interval) throws IllegalStateException {
		getOrLoadAsyncProfiler().start(event, interval);
	}

	@Override
	public void resume(String event, long interval) throws IllegalStateException {
		getOrLoadAsyncProfiler().resume(event, interval);
	}

	@Override
	public void stop() throws IllegalStateException {
		getOrLoadAsyncProfiler().stop();
	}

	@Override
	public long getSamples() {
		return getOrLoadAsyncProfiler().getSamples();
	}

	@Override
	public String getVersion() {
		return getOrLoadAsyncProfiler().getVersion();
	}

	@Override
	public String execute(String command) throws IOException {
		return getOrLoadAsyncProfiler().execute(command);
	};

	@Override
	public String dumpCollapsed(Counter counter) {
		return getOrLoadAsyncProfiler().dumpCollapsed(counter);
	}

	@Override
	public String dumpTraces(int maxTraces) {
		return getOrLoadAsyncProfiler().dumpTraces(maxTraces);
	}

	@Override
	public String dumpFlat(int maxMethods) {
		return getOrLoadAsyncProfiler().dumpFlat(maxMethods);
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
