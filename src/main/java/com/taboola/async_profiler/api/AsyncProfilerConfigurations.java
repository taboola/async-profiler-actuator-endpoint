package com.taboola.async_profiler.api;

import com.taboola.async_profiler.api.continuous.ContinuousProfilingSnapshotRequest;
import com.taboola.async_profiler.api.continuous.pyroscope.PyroscopeReporterConfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class AsyncProfilerConfigurations {
    String libPath = null;
    String profileTempFileName = "async_profiler_endpoint_dump";
    ContinuousProfilingConfig continuousProfiling = new ContinuousProfilingConfig();


    @Data
    public static class ContinuousProfilingConfig {
        boolean startOnInit = false;
        long failureBackoffSeconds = 10;
        ContinuousProfilingSnapshotRequest snapshotRequest = new ContinuousProfilingSnapshotRequest();
        ExecutorServiceConfig snapshotsReporterExecutorService = new ExecutorServiceConfig(1, 1, 5);
        PyroscopeReporterConfig pyroscopeReporter = new PyroscopeReporterConfig();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutorServiceConfig {
        int corePoolSize;
        int maxPoolSize;
        int queueCapacity;
    }
}
