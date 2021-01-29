package com.taboola.async_profiler.api.facade;

import com.taboola.async_profiler.api.original.Events;

import lombok.Data;

@Data
public class ProfileRequest {

    private int durationSeconds = 60;//profiling duration
    private int frameBufferSize = 5_000_000;
    private Integer samplingIntervalMs = 10;//10ms default, relevant only for non alloc events.
    private Integer samplingIntervalBytes = 10_000_000;//relevant only for alloc event.
    private boolean separateThreads = false;
    private String eventType = Events.CPU;
    private String format = "svg";
    private String includedThreads;
    private String includedTraces;
    private String excludedTraces;

    public boolean hasIncludedThreads() {
        return includedThreads != null && !includedThreads.equals("");
    }

    public boolean isFlameGraphRequest() {
        return "svg".equals(format) || "flamegraph".equals(format);
    }
}