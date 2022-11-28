package com.taboola.async_profiler.api.facade;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.taboola.async_profiler.api.original.Events;
import com.taboola.async_profiler.api.original.Format;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    Set<String> events = new HashSet<String>(){{add(Events.CPU);}};
    Format format = Format.FLAMEGRAPH;
    int durationSeconds = 60;//profiling duration
    Integer samplingInterval = 1;
    TimeUnit samplingIntervalTimeUnit = TimeUnit.MILLISECONDS;
    Integer allocIntervalBytes = 10_000;//relevant only for alloc event.
    boolean liveObjectsOnly = false;
    Integer lockThresholdNanos = 1;//relevant only for lock event.
    boolean separateThreads = false;
    String includedThreads;
    String includedTraces;
    String excludedTraces;
    String jfrSync;

    public Format getFormat() {
        if (events != null && events.size() > 1) {
            //when profiling multiple events together only jfr is supported, convert automatically
            return Format.JFR;
        }

        return format;
    }
}
