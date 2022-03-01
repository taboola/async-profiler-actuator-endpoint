package com.taboola.async_profiler.api;

import lombok.SneakyThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taboola.async_profiler.api.continuous.ContinuousProfilingSnapshotRequest;
import com.taboola.async_profiler.api.continuous.ProfileResultsReporter;
import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;
import com.taboola.async_profiler.api.serviceconfig.AsyncProfilerServiceConfigurations;
import com.taboola.async_profiler.utils.RecurringRunnable;
import com.taboola.async_profiler.utils.ThreadUtils;

public class AsyncProfilerService {

    private static Logger logger = LoggerFactory.getLogger(AsyncProfilerService.class);

    private final AsyncProfilerFacade asyncProfilerFacade;
    private final ProfileResultsReporter profileResultsReporter;
    private final ExecutorService continuousProfilingExecutorService;
    private final ExecutorService snapshotsReporterExecutorService;
    private final long continuousProfilingFailureBackoffSeconds;
    private final ThreadUtils threadUtils;
    private final Object lock;
    private RecurringRunnable currentContinuousProfilingTask;

    public AsyncProfilerService(AsyncProfilerFacade asyncProfilerFacade,
                                ProfileResultsReporter profileResultsReporter,
                                ExecutorService continuousProfilingExecutorService,
                                ExecutorService snapshotsReporterExecutorService,
                                AsyncProfilerServiceConfigurations asyncProfilerConfigurations,
                                ThreadUtils threadUtils) {
        this.asyncProfilerFacade = asyncProfilerFacade;
        this.profileResultsReporter = profileResultsReporter;
        this.continuousProfilingExecutorService = continuousProfilingExecutorService;
        this.snapshotsReporterExecutorService = snapshotsReporterExecutorService;
        this.continuousProfilingFailureBackoffSeconds = asyncProfilerConfigurations.getContinuousProfiling().getFailureBackoffSeconds();
        this.threadUtils = threadUtils;
        this.lock = new Object();

        if (asyncProfilerConfigurations.getContinuousProfiling().isStartOnInit()) {
            startContinuousProfiling(asyncProfilerConfigurations.getContinuousProfiling().getSnapshotRequest());
        }
    }

    public ProfileResult profile(ProfileRequest profileRequest) {
        return asyncProfilerFacade.profile(profileRequest);
    }

    public ProfileResult stop() {
        return asyncProfilerFacade.stop();
    }

    public void startContinuousProfiling(ContinuousProfilingSnapshotRequest continuousProfilingSnapshotRequest) {
        synchronized (lock) {
            if (currentContinuousProfilingTask == null) {
                RecurringRunnable continuousProfilingTask = new RecurringRunnable(() -> profileAndReportSnapshot(continuousProfilingSnapshotRequest));
                continuousProfilingExecutorService.submit(continuousProfilingTask);
                currentContinuousProfilingTask = continuousProfilingTask;
            } else {
                throw new IllegalStateException("Failed starting continuous profiling, there is already an active session");
            }
        }
    }

    public void stopContinuousProfiling() {
        synchronized (lock) {
            if (currentContinuousProfilingTask != null) {
                currentContinuousProfilingTask.cancel();
                currentContinuousProfilingTask = null;
                safeStop();
            } else {
                throw new IllegalStateException("There is no active continuous profiling session");
            }
        }
    }

    public String getSupportedEvents() {
        return asyncProfilerFacade.getSupportedEvents();
    }

    public String getProfilerVersion() {
        return asyncProfilerFacade.getProfilerVersion();
    }

    @SneakyThrows
    private void profileAndReportSnapshot(ContinuousProfilingSnapshotRequest continuousProfilingSnapshotRequest) {
        try {
            ProfileResult profileSnapshotResult = asyncProfilerFacade.profile(continuousProfilingSnapshotRequest);
            reportAsync(profileSnapshotResult);

        } catch (RuntimeException ex) {
            logger.error("Failed submitting profile result, sleeping for {} seconds", ex, continuousProfilingFailureBackoffSeconds);
            //sleep for backoff seconds if the profiler has failed or if we failed to submit the result to the reporter executor service
            //we will throw from here only if we failed to sleep because it was interrupted, which is ok
            threadUtils.sleep(continuousProfilingFailureBackoffSeconds, TimeUnit.SECONDS);
        }
    }

    private void reportAsync(ProfileResult profileSnapshotResult) {
        try {
            snapshotsReporterExecutorService.submit(() -> reportSnapshot(profileSnapshotResult));
        } catch (Exception ex) {
            if (profileSnapshotResult != null) {
                try {
                    profileSnapshotResult.close();
                } catch (Exception e) {
                    logger.error("Failed closing profile result", e);
                }
            }

            throw new RuntimeException(ex);
        }
    }

    private void reportSnapshot(ProfileResult snapshotResult) {
        try (ProfileResult profileResult = snapshotResult) {
            profileResultsReporter.report(profileResult);
        } catch (Exception ex) {
            logger.error("Unexpected error in profile results reporter", ex);
        }
    }

    private void safeStop() {
        try {
            stop();
        } catch (RuntimeException e) {}
    }
}
