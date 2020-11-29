package com.taboola.async_profiler.api.facade;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.taboola.async_profiler.api.facade.profiler.AsyncProfiler;
import com.taboola.async_profiler.utils.IOUtils;
import com.taboola.async_profiler.utils.ThreadUtils;

/**
 * A friendlier API over the original AsyncProfiler.
 * */
public class AsyncProfilerFacade {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AsyncProfiler asyncProfiler;
    private final AsyncProfilerCommandsFactory asyncProfilerCommandsFactory;
    private final AsyncProfilerFacadeConfig facadeConfig;
    private final ThreadUtils threadUtils;
    private final IOUtils ioUtils;
    private final AtomicReference<ProfileContext> referenceToCurrentProfileRequestContext;

    public AsyncProfilerFacade(AsyncProfiler asyncProfiler,
                               AsyncProfilerFacadeConfig asyncProfilerFacadeConfig,
                               AsyncProfilerCommandsFactory asyncProfilerCommandsFactory,
                               ThreadUtils threadUtils,
                               IOUtils ioUtils) {
        this.asyncProfiler = asyncProfiler;
        this.facadeConfig = asyncProfilerFacadeConfig;
        this.asyncProfilerCommandsFactory = asyncProfilerCommandsFactory;
        this.threadUtils = threadUtils;
        this.ioUtils = ioUtils;
        this.referenceToCurrentProfileRequestContext = new AtomicReference<>(null);
    }

    /**
     * Execute a profile request and dump the result into the given OutputStream.
     * */
    public void profile(ProfileRequest profileRequest, OutputStream outputStream) {
        ProfileContext profileContext = null;
        try {
            profileContext = new ProfileContext(profileRequest, LocalDateTime.now(), ioUtils.createTempFile(facadeConfig.getProfileTempFileName(), ".tmp"), Thread.currentThread());
            //We store the current context (in an atomic reference) in order to:
            //1. Allow a single profile request at a time
            //2. Allow stopping the current session from a separate request
            if (referenceToCurrentProfileRequestContext.compareAndSet(null, profileContext)) {
                try {
                    profileInternal(profileContext, outputStream);
                } finally {
                    referenceToCurrentProfileRequestContext.compareAndSet(profileContext, null);
                }
            } else {
                throw new IllegalStateException("Failed getting a profiling session, another one is already running: " + referenceToCurrentProfileRequestContext.get());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (profileContext != null) {
                ioUtils.safeDeleteIfExists(profileContext.getTmpFilePath());
            }
        }
    }

    /**
     * Try stopping the currently running profile request, if any, and dump the result into the given OutputStream.
     * */
    public void stop(OutputStream outputStream) {
        ProfileContext originalProfileContext = getCurrentProfileRequestContext();
        try {
            if (originalProfileContext != null) {
                try {
                    stopInternal(originalProfileContext, outputStream);
                } finally {
                    ioUtils.safeDeleteIfExists(originalProfileContext.getTmpFilePath());
                    originalProfileContext.getRequestThread().interrupt();//if the original thread is sleeping, interrupt it
                }
            } else {
                throw new IllegalStateException("There is no active profiling session to stop");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the supported event types for the current run-time env
     * */
    public String getSupportedEvents() {
        try {
            return asyncProfiler.execute(asyncProfilerCommandsFactory.createGetSupportedEventsCommand());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getProfilerVersion() {
        try {
            return asyncProfiler.execute(asyncProfilerCommandsFactory.createGetVersionCommand());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ProfileContext getCurrentProfileRequestContext() {
        return referenceToCurrentProfileRequestContext.get();
    }

    private void profileInternal(ProfileContext profileContext, OutputStream outputStream) throws IOException, InterruptedException {
        ProfileRequest profileRequest = profileContext.getProfileRequest();
        String tmpFilePath = profileContext.getTmpFilePath();
        String startCommandResponse = asyncProfiler.execute(asyncProfilerCommandsFactory.createStartCommand(profileRequest, tmpFilePath));
        if (startedSuccessfully(startCommandResponse)) {
            profileSpecificThreadsIfNeeded(profileRequest);
            threadUtils.sleep(profileRequest.getDurationSeconds(), TimeUnit.SECONDS);
            stopInternal(profileContext, outputStream);
        } else {
            throw new IllegalStateException("Failed starting the profiler: " + startCommandResponse);
        }
    }

    private void stopInternal(ProfileContext profileContext, OutputStream outputStream) throws IOException {
        String stopCommandResponse = asyncProfiler.execute(asyncProfilerCommandsFactory.createStopCommand(profileContext.getProfileRequest(), profileContext.getTmpFilePath(), buildTitleIfNeeded(profileContext.getProfileRequest(), profileContext.getStartTime())));

        if (stoppedSuccessfully(stopCommandResponse)) {
            ioUtils.copyFileContent(profileContext.getTmpFilePath(), outputStream);
        } else {
            throw new IllegalStateException("Failed stopping the profiler: " + stopCommandResponse);
        }
    }

    private String buildTitleIfNeeded(ProfileRequest profileRequest, LocalDateTime startTime) {
        String title = null;
        if (profileRequest.isFlameGraphRequest()) {
            title = String.format("%s Flame Graph (%s - %s)", profileRequest.getEventType().toUpperCase(), startTime.format(formatter), LocalDateTime.now().format(formatter));
        }
        return title;
    }

    private void profileSpecificThreadsIfNeeded(ProfileRequest profileRequest) {
        if (profileRequest.hasIncludedThreads()) {
            String includedThreadsName = profileRequest.getIncludedThreads();
            Collection<Thread> monitoredThreads = threadUtils.getAllThreads(thread -> shouldIncludeThread(thread, includedThreadsName));

            for (Thread thread : monitoredThreads) {
                asyncProfiler.addThread(thread);
            }
        }
    }

    private boolean startedSuccessfully(String startCommandResponse) {
        return startCommandResponse.startsWith(facadeConfig.getSuccessfulStartCommandResponse());
    }

    private boolean stoppedSuccessfully(String stopCommandResponse) {
        return stopCommandResponse.startsWith(facadeConfig.getSuccessfulStopCommandResponse());
    }

    private static boolean shouldIncludeThread(Thread thread, String includedThreadsName) {
        return thread.getName() != null && thread.getName().contains(includedThreadsName);
    }
}