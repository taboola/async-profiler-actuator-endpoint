package com.taboola.async_profiler.api.continuous;

import com.taboola.async_profiler.api.facade.ProfileResult;

public interface ProfileResultsReporter {
    void report(ProfileResult profileResult);
}
