package com.taboola.async_profiler.api.continuous.pyroscope;

import lombok.Data;

@Data
public class PyroscopeReporterConfig {
    String pyroscopeServerAddress;//e.g: "http://localhost:4040"
    String appName;
    String pyroscopeServerIngestPath = "/ingest";
    String spyName = "asyncProfilerActuatorEndpoint";
    int connectTimeout = 30_000;
    int readTimeout = 30_000;
}
