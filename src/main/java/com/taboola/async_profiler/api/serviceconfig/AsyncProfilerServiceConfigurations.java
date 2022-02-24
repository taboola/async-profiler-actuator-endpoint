package com.taboola.async_profiler.api.serviceconfig;

import lombok.Data;

@Data
public class AsyncProfilerServiceConfigurations {
    String libPath = null;
    String profileTempFileName = "async_profiler_endpoint_dump";
    ContinuousProfilingConfig continuousProfiling = new ContinuousProfilingConfig();
}
