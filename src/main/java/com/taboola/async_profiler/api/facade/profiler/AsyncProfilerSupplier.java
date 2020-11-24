package com.taboola.async_profiler.api.facade.profiler;

import com.taboola.async_profiler.api.original.AsyncProfilerImpl;

public class AsyncProfilerSupplier {

    private AsyncProfiler asyncProfiler;

    public AsyncProfilerSupplier(String libPath) {
        try {
            asyncProfiler = AsyncProfilerImpl.getInstance(libPath);
        } catch (Throwable t) {
            //Couldn't load the profiler library, use the empty implementation
            asyncProfiler = new EmptyAsyncProfiler();
        }
    }

    /**
     *  @return the actual {@link AsyncProfilerImpl} when the native lib was loaded successfully (it is present and valid),
     *  otherwise return an {@link EmptyAsyncProfiler}.
     *  */
    public AsyncProfiler getProfiler() {
        return asyncProfiler;
    }

}
