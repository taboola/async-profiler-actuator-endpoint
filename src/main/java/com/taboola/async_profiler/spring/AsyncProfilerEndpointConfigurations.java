package com.taboola.async_profiler.spring;

import lombok.Data;

@Data
public class AsyncProfilerEndpointConfigurations {
    private String libPath = null;
    private String profileTempFileName = "async_profiler_endpoint_dump";
    private boolean sensitiveEndpoint = false;
}
