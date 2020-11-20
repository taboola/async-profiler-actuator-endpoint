package com.taboola.async_profiler.api.facade.profiler;

import com.taboola.async_profiler.api.original.AsyncProfiler;

public class ProfilerProvider {

    private AsyncProfilerInterface asyncProfiler;

    public ProfilerProvider(String libPath) {
        try {
            asyncProfiler = AsyncProfiler.getInstance(libPath);
        } catch (Throwable t) {
            //Couldn't load the profiler library, use the empty implementation
            asyncProfiler = new EmptyAsyncProfiler();
        }
    }

    public AsyncProfilerInterface getProfiler() {
        return asyncProfiler;
    }

}
