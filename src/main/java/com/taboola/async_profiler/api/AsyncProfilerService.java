package com.taboola.async_profiler.api;

import lombok.SneakyThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.taboola.async_profiler.api.continuous.ProfileSnapshotsReporter;
import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;
import com.taboola.async_profiler.utils.RecurringRunnable;
import com.taboola.async_profiler.utils.ThreadUtils;

public class AsyncProfilerService {

    private final AsyncProfilerFacade asyncProfilerFacade;
    private final ProfileSnapshotsReporter snapshotsReporter;
    private final ExecutorService continuousProfilingExecutorService;
    private final ExecutorService snapshotsReporterExecutorService;
    private final long continuousProfilingFailureBackoffSeconds;
    private final ThreadUtils threadUtils;
    private final Object lock;
    private RecurringRunnable currentContinuousProfilingTask;

    public AsyncProfilerService(AsyncProfilerFacade asyncProfilerFacade,
                                ProfileSnapshotsReporter snapshotsReporter,
                                ExecutorService continuousProfilingExecutorService,
                                ExecutorService snapshotsReporterExecutorService,
                                AsyncProfilerConfigurations asyncProfilerConfigurations,
                                ThreadUtils threadUtils) {
        this.asyncProfilerFacade = asyncProfilerFacade;
        this.snapshotsReporter = snapshotsReporter;
        this.continuousProfilingExecutorService = continuousProfilingExecutorService;
        this.snapshotsReporterExecutorService = snapshotsReporterExecutorService;
        this.continuousProfilingFailureBackoffSeconds = asyncProfilerConfigurations.getContinuousProfiling().getFailureBackoffSeconds();
        this.threadUtils = threadUtils;
        this.lock = new Object();
    }

    public ProfileResult profile(ProfileRequest profileRequest) {
        return asyncProfilerFacade.profile(profileRequest);
    }

    public ProfileResult stop() {
        return asyncProfilerFacade.stop();
    }

    public void startContinuousProfiling(ProfileRequest profileSnapshotRequest) {
        synchronized (lock) {
            if (currentContinuousProfilingTask == null) {
                RecurringRunnable profilingTask = new RecurringRunnable(() -> getProfileSnapshotAndSubmitToReporter(profileSnapshotRequest));
                continuousProfilingExecutorService.submit(profilingTask);
                currentContinuousProfilingTask = profilingTask;
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
    private void getProfileSnapshotAndSubmitToReporter(ProfileRequest profileSnapshotRequest) {
        try {
            ProfileResult profileSnapshotResult = asyncProfilerFacade.profile(profileSnapshotRequest);
            submitToReporter(profileSnapshotResult);

        } catch (RuntimeException ex) {
            //sleep for backoff seconds if the profiler has failed or if we failed to submit the result to the reporter executor service
            //we will throw from here only if we failed to sleep because it was interrupted, which is ok
            threadUtils.sleep(continuousProfilingFailureBackoffSeconds, TimeUnit.SECONDS);
        }
    }

    private void submitToReporter(ProfileResult profileSnapshotResult) {
        try {
            snapshotsReporterExecutorService.submit(() -> reportSnapshot(profileSnapshotResult));
        } catch (Exception ex) {
            if (profileSnapshotResult != null) {
                try {
                    profileSnapshotResult.close();
                } catch (Exception e) {
                }
            }

            throw new RuntimeException(ex);
        }
    }

    private void reportSnapshot(ProfileResult snapshotResult) {
        try (ProfileResult profileResult = snapshotResult) {
            snapshotsReporter.report(profileResult);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
