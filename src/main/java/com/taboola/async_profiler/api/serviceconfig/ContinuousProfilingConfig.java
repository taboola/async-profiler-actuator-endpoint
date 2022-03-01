package com.taboola.async_profiler.api.serviceconfig;

import lombok.Data;

import com.taboola.async_profiler.api.continuous.ContinuousProfilingSnapshotRequest;
import com.taboola.async_profiler.api.continuous.pyroscope.PyroscopeReporterConfig;

@Data
public class ContinuousProfilingConfig {
    boolean startOnInit = false;
    long failureBackoffSeconds = 10;
    ContinuousProfilingSnapshotRequest snapshotRequest = new ContinuousProfilingSnapshotRequest();
    ExecutorServiceConfig snapshotsReporterExecutorService = new ExecutorServiceConfig(1, 1, 5);
    PyroscopeReporterConfig pyroscopeReporter = new PyroscopeReporterConfig();
}
