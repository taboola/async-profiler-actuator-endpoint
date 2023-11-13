package com.taboola.async_profiler.api.continuous.pyroscope;

import lombok.Data;

import java.util.zip.Deflater;

@Data
public class PyroscopeReporterConfig {
    String pyroscopeServerAddress;//e.g: "http://localhost:4040"
    String appName;
    String pyroscopeServerIngestPath = "/ingest";
    String spyName = "asyncProfilerActuatorEndpoint";
    int connectTimeoutMillis = 30_000;
    int readTimeoutMillis = 30_000;
    int callTimeoutMillis = 30_000;
    int compressionLevelJFR = Deflater.NO_COMPRESSION;
}
