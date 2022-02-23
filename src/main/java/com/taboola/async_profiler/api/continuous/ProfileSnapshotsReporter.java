package com.taboola.async_profiler.api.continuous;

import com.taboola.async_profiler.api.facade.ProfileResult;

public interface ProfileSnapshotsReporter {
    void report(ProfileResult snapshotProfileResult);
}
