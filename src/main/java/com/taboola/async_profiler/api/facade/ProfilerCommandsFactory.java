package com.taboola.async_profiler.api.facade;

import java.util.concurrent.TimeUnit;

import com.taboola.async_profiler.api.original.Events;

/**
 * Factory for AsyncProfiler string commands
 * */
public class ProfileCommandFactory {

    public String createProfileCommand(ProfileRequest profileRequest) {
        validate(profileRequest);

        StringBuilder stringBuilder = new StringBuilder("start");

        stringBuilder.append(",event=");
        stringBuilder.append(profileRequest.getEventType());

        Integer interval = getInterval(profileRequest);
        if (interval != null) {
            stringBuilder.append(",interval=");
            stringBuilder.append(interval);
        }

        stringBuilder.append(",framebuf=");
        stringBuilder.append(profileRequest.getFrameBufferSize());

        if (profileRequest.hasIncludedThreads()) {
            stringBuilder.append(",filter");//need to pass the filter flag when using the java threads filtering api
        }

        return stringBuilder.toString();
    }

    public String createGetSupportedEventsCommand() {
        return "list";
    }

    private Integer getInterval(ProfileRequest profileRequest) {
        Integer interval = null;
        if (profileRequest.getEventType().equals(Events.ALLOC)) {
            interval = profileRequest.getSamplingIntervalBytes();
        } else {
            if (profileRequest.getSamplingIntervalMs() != null) {
                interval = Math.toIntExact(TimeUnit.MILLISECONDS.toNanos(profileRequest.getSamplingIntervalMs())); //interval should be in nanos
            }
        }

        return interval;
    }

    private void validate(ProfileRequest profileRequest) {
        if (profileRequest.getEventType() == null || profileRequest.getEventType().equals("")) {
            throw new IllegalArgumentException("Event type is required");
        }

        if (profileRequest.getDurationSeconds() <= 0) {
            throw new IllegalArgumentException("Profiling duration must be greater than 0");
        }

        if (profileRequest.getSamplingIntervalMs() != null && profileRequest.getSamplingIntervalMs() <= 0) {
            throw new IllegalArgumentException("Sampling interval must be greater than 0");
        }

        if (profileRequest.getSamplingIntervalBytes() != null && profileRequest.getSamplingIntervalBytes() <= 0) {
            throw new IllegalArgumentException("Sampling interval must be greater than 0");
        }

        if (profileRequest.getFrameBufferSize() <= 0) {
            throw new IllegalArgumentException("Frame buffer size must be greater than 0");
        }
    }
}
