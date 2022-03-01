package com.taboola.async_profiler.api.continuous;

import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.original.Format;

public class ContinuousProfilingSnapshotRequest extends ProfileRequest {

    public ContinuousProfilingSnapshotRequest() {
        setFormat(Format.COLLAPSED);
        setDurationSeconds(10);
    }
}
