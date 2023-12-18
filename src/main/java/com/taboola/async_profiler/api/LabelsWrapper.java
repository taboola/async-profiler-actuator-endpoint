package com.taboola.async_profiler.api;

import io.pyroscope.labels.LabelsSet;
import io.pyroscope.labels.Pyroscope;

import java.util.concurrent.Callable;

public class LabelsWrapper {
    private static AsyncProfilerService asyncProfilerService;

    public static void setAsyncProfilerService(AsyncProfilerService asyncProfilerService) {
        LabelsWrapper.asyncProfilerService = asyncProfilerService;
    }

    public static <T> T run(LabelsSet labels, Callable<T> c) throws Exception {
        if (asyncProfilerService != null && asyncProfilerService.isContinuousProfilingActive()) {
            return Pyroscope.LabelsWrapper.run(labels, c);
        } else {
            return c.call();
        }
    }

    public static void run(LabelsSet labels, Runnable c) {
        if (asyncProfilerService != null && asyncProfilerService.isContinuousProfilingActive()) {
            Pyroscope.LabelsWrapper.run(labels, c);
        } else {
            c.run();
        }
    }
}
