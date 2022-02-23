package com.taboola.async_profiler.api.facade;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.taboola.async_profiler.api.facade.profiler.AsyncProfiler;
import com.taboola.async_profiler.api.original.Format;
import com.taboola.async_profiler.utils.IOUtils;
import com.taboola.async_profiler.utils.ThreadUtils;

/**
 * A friendlier API over the original AsyncProfiler.
 * */
public class AsyncProfilerFacade {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AsyncProfiler asyncProfiler;
    private final AsyncProfilerCommandsFactory asyncProfilerCommandsFactory;
    private final String tempFileName;
    private final ThreadUtils threadUtils;
    private final IOUtils ioUtils;
    private final AtomicReference<ProfileContext> referenceToCurrentProfileRequestContext;

    public AsyncProfilerFacade(AsyncProfiler asyncProfiler,
                               String tempFileName,
                               AsyncProfilerCommandsFactory asyncProfilerCommandsFactory,
                               ThreadUtils threadUtils,
                               IOUtils ioUtils) {
        this.asyncProfiler = asyncProfiler;
        this.tempFileName = tempFileName;
        this.asyncProfilerCommandsFactory = asyncProfilerCommandsFactory;
        this.threadUtils = threadUtils;
        this.ioUtils = ioUtils;
        this.referenceToCurrentProfileRequestContext = new AtomicReference<>(null);
    }

    /**
     * Execute a profile request.
     * @param profileRequest contains profiling configuration for the current session
     * @return a {@link ProfileResult}
     * */
    public ProfileResult profile(ProfileRequest profileRequest) {
        ProfileContext profileContext = null;
        try {
            profileContext = new ProfileContext(profileRequest, LocalDateTime.now(), ioUtils.createTempFile(tempFileName, ".tmp"), Thread.currentThread());
            //We store the current context (in an atomic reference) in order to:
            //1. Allow a single profile request at a time
            //2. Allow stopping the current session from a separate request
            if (referenceToCurrentProfileRequestContext.compareAndSet(null, profileContext)) {
                try {
                    return profileInternal(profileContext);
                } finally {
                    referenceToCurrentProfileRequestContext.compareAndSet(profileContext, null);
                }
            } else {
                throw new IllegalStateException("Failed getting a profiling session, another one is already running: " + referenceToCurrentProfileRequestContext.get());
            }
        } catch (Exception e) {
            if (profileContext != null) {
                ioUtils.safeDeleteIfExists(profileContext.getTmpFilePath());
            }

            throw new RuntimeException("Unexpected failure occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Try stopping the currently running profile request, if any.
     * @return the {@link ProfileResult} of the stopped session
     * */
    public ProfileResult stop() {
        ProfileContext originalProfileContext = getCurrentProfileRequestContext();
        if (originalProfileContext != null) {
            try {
                return stopInternal(originalProfileContext);
            } catch (Exception ex) {
                ioUtils.safeDeleteIfExists(originalProfileContext.getTmpFilePath());
                throw new IllegalStateException("Failed stopping the current session: " + ex.getMessage(), ex);
            } finally {
                originalProfileContext.getRequestThread().interrupt();//if the original thread is sleeping, interrupt it
            }
        } else {
            throw new IllegalStateException("There is no active profiling session to stop");
        }
    }

    /**
     * @return the supported event types for the current run-time env
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

    private ProfileResult profileInternal(ProfileContext profileContext) throws IOException, InterruptedException {
        ProfileRequest profileRequest = profileContext.getProfileRequest();
        String tmpFilePath = profileContext.getTmpFilePath();
        asyncProfiler.execute(asyncProfilerCommandsFactory.createStartCommand(profileRequest, tmpFilePath));
        profileSpecificThreadsIfNeeded(profileRequest);

        threadUtils.sleep(profileRequest.getDurationSeconds(), TimeUnit.SECONDS);

        return stopInternal(profileContext);
    }

    private ProfileResult stopInternal(ProfileContext profileContext) throws IOException {
        LocalDateTime endTime = LocalDateTime.now();
        String stopCommand = asyncProfilerCommandsFactory.createStopCommand(profileContext.getProfileRequest(),
                profileContext.getTmpFilePath(),
                buildTitleIfNeeded(profileContext.getProfileRequest(), profileContext.getStartTime(), endTime));

        asyncProfiler.execute(stopCommand);

        InputStream resultInputStream = ioUtils.getDisposableFileInputStream(profileContext.getTmpFilePath());
        return new ProfileResult(profileContext.getProfileRequest(),
                resultInputStream,
                profileContext.getStartTime(),
                endTime);
    }

    private String buildTitleIfNeeded(ProfileRequest profileRequest, LocalDateTime startTime, LocalDateTime endTime) {
        String title = null;
        if (Format.FLAMEGRAPH.equals(profileRequest.getFormat())) {
            title = String.format("%s Flame Graph (%s - %s)", profileRequest.getEvents(), startTime.format(formatter), endTime.format(formatter));
        }
        return title;
    }

    private void profileSpecificThreadsIfNeeded(ProfileRequest profileRequest) {
        if (profileRequest.getIncludedThreads() != null) {
            String includedThreadsName = profileRequest.getIncludedThreads();
            Collection<Thread> monitoredThreads = threadUtils.getAllThreads(thread -> shouldIncludeThread(thread, includedThreadsName));

            for (Thread thread : monitoredThreads) {
                asyncProfiler.addThread(thread);
            }
        }
    }

    private static boolean shouldIncludeThread(Thread thread, String includedThreadsName) {
        return thread.getName() != null && thread.getName().contains(includedThreadsName);
    }
}