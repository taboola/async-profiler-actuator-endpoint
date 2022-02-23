package com.taboola.async_profiler.api.continuous;

import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.original.Format;

public class ContinuousProfilingSnapshotRequest extends ProfileRequest {

    //Basically extending ProfileRequest just to override the defaults
    public ContinuousProfilingSnapshotRequest() {
        setFormat(Format.COLLAPSED);
        setDurationSeconds(10);
    }
}
