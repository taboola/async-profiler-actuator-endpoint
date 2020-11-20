package com.taboola.async_profiler.api.facade;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.taboola.async_profiler.api.facade.profiler.AsyncProfiler;
import com.taboola.async_profiler.api.original.Counter;
import com.taboola.async_profiler.utils.ThreadUtils;

/**
 * A friendlier API layer over the original async-profiler components.
 * */
public class AsyncProfilerFacade {

    private static final String PROFILE_COMMAND_SUCCESS_RESPONSE = "Started";
    private static final int PROFILE_LOCK_MAX_WAITING_TIME_SECONDS = 1;

    private final AsyncProfiler asyncProfiler;
    private final ProfilerCommandsFactory profilerCommandsFactory;
    private final FlameGraphFactory flameGraphFactory;
    private final ThreadUtils threadUtils;
    private final Lock profileLock;

    public AsyncProfilerFacade(AsyncProfiler asyncProfiler,
                               ProfilerCommandsFactory profilerCommandsFactory,
                               FlameGraphFactory flameGraphFactory,
                               ThreadUtils threadUtils) {
        this.asyncProfiler = asyncProfiler;
        this.profilerCommandsFactory = profilerCommandsFactory;
        this.flameGraphFactory = flameGraphFactory;
        this.threadUtils = threadUtils;
        this.profileLock = new ReentrantLock();
    }

    /**
     * Execute profile request and return the result as a flame graph.
     * */
    public String profile(ProfileRequest profileRequest) {
        try {
            if (profileLock.tryLock(PROFILE_LOCK_MAX_WAITING_TIME_SECONDS, TimeUnit.SECONDS)) {
                try {
                    return profileInternal(profileRequest);
                } finally {
                    profileLock.unlock();
                }
            } else {
                throw new IllegalStateException("Failed getting a profiling session, another one is already running");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the supported event types for the current run-time env
     * */
    public String getSupportedEvents() {
        try {
            return asyncProfiler.execute(profilerCommandsFactory.createGetSupportedEventsCommand());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String profileInternal(ProfileRequest profileRequest) throws IOException, InterruptedException {
        String startCommand = profilerCommandsFactory.createStartCommand(profileRequest);
        String startCommandResponse = asyncProfiler.execute(startCommand);
        if (startedSuccessfully(startCommandResponse)) {
            monitorSpecificThreadsIfNeeded(profileRequest);
            Thread.sleep(TimeUnit.SECONDS.toMillis(profileRequest.getDurationSeconds()));
            asyncProfiler.stop();
            String collapsedStackTracesResult = asyncProfiler.dumpCollapsed(Counter.TOTAL);

            return flameGraphFactory.createFlameGraph(collapsedStackTracesResult, trace -> shouldIncludeTrace(profileRequest, trace));

        } else {
            //Didn't get a successful response, there's probably another profiling session that is already running
            throw new IllegalStateException("Failed executing profile command: " + startCommandResponse);
        }
    }

    private void monitorSpecificThreadsIfNeeded(ProfileRequest profileRequest) {
        if (profileRequest.hasIncludedThreads()) {
            String includedThreadsName = profileRequest.getIncludedThreads();
            Collection<Thread> monitoredThreads = threadUtils.getAllThreads(thread -> shouldIncludeThread(thread, includedThreadsName));

            for (Thread thread : monitoredThreads) {
                asyncProfiler.addThread(thread);
            }
        }
    }

    private static boolean startedSuccessfully(String startCommandResponse) {
        return startCommandResponse.startsWith(PROFILE_COMMAND_SUCCESS_RESPONSE);
    }

    private static boolean shouldIncludeTrace(ProfileRequest profileRequest, String collapsedTrace) {
        boolean isIncluded = profileRequest.getIncludedTraces() == null || collapsedTrace.contains(profileRequest.getIncludedTraces());
        boolean isExcluded = profileRequest.getExcludedTraces() != null && collapsedTrace.contains(profileRequest.getExcludedTraces());

        return isIncluded && !isExcluded;
    }

    private static boolean shouldIncludeThread(Thread thread, String includedThreadsName) {
        return thread.getName() != null && thread.getName().contains(includedThreadsName);
    }
}