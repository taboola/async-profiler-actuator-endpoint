package com.taboola.async_profiler.api.facade;

import lombok.Data;

@Data
public class AsyncProfilerFacadeConfig {
    private String profileTempFileName = "async_profiler_endpoint_dump";
    private String successfulStartCommandResponse = "OK";
    private String successfulStopCommandResponse = "OK";
}
