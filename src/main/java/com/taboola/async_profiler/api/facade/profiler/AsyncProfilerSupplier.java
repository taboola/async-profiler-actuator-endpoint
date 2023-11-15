package com.taboola.async_profiler.api.facade.profiler;

import io.pyroscope.labels.io.pyroscope.PyroscopeAsyncProfiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncProfilerSupplier {

	private static Logger logger = LoggerFactory.getLogger(AsyncProfilerSupplier.class);

	private final AsyncProfiler asyncProfiler;

	public AsyncProfilerSupplier() {
		asyncProfiler = new LazyLoadedAsyncProfiler(this::loadAsyncProfilerLib);
	}

	public AsyncProfiler getProfiler() {
		return asyncProfiler;
	}

    private AsyncProfiler loadAsyncProfilerLib() {
        try {
			return new AsyncProfiler(PyroscopeAsyncProfiler.getAsyncProfiler());
        } catch (Throwable t) {
            logger.error("Failed loading async profiler lib", t);
            //Couldn't load the profiler library, use the empty implementation
            return new EmptyAsyncProfiler(t);
        }
    }
}
