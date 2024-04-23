package com.taboola.async_profiler.api.facade;

import com.taboola.async_profiler.api.original.Events;

/**
 * Factory for AsyncProfiler string commands
 * */
public class AsyncProfilerCommandsFactory {

    public String createStartCommand(ProfileRequest profileRequest, String filePath) {
        validate(profileRequest, filePath);

        StringBuilder stringBuilder = new StringBuilder("start");

        stringBuilder.append(",event=").append(String.join(",", profileRequest.getEvents()));

        if (profileRequest.getEvents().contains(Events.ALLOC) && profileRequest.getAllocIntervalBytes() != null) {
            stringBuilder.append(",alloc=");
            stringBuilder.append(profileRequest.getAllocIntervalBytes());
        }

        if (profileRequest.getEvents().contains(Events.LOCK) && profileRequest.getLockThresholdNanos() != null) {
            stringBuilder.append(",lock=");
            stringBuilder.append(profileRequest.getLockThresholdNanos());
        }

        appendFileAndFormat(profileRequest, filePath, stringBuilder);

        if (profileRequest.getSamplingInterval() != null && profileRequest.getSamplingIntervalTimeUnit() != null) {
            int interval = Math.toIntExact(profileRequest.getSamplingIntervalTimeUnit().toNanos(profileRequest.getSamplingInterval())); //interval should be in nanos
            stringBuilder.append(",interval=");
            stringBuilder.append(interval);
        }

        if (profileRequest.getEvents().contains(Events.ALLOC) && profileRequest.isLiveObjectsOnly()) {
            stringBuilder.append(",live");
        }

        if (profileRequest.getJfrSync() != null) {
            if (profileRequest.getJfrSync().isEmpty()) {
                stringBuilder.append(",jfrsync");
            } else {
                stringBuilder.append(",jfrsync=");
                stringBuilder.append(profileRequest.getJfrSync());
            }
        }

        if (profileRequest.getIncludedThreads() != null) {
            stringBuilder.append(",filter"); //need to pass the filter flag when using the java threads filtering api
        }

        if (profileRequest.isSeparateThreads()) {
            stringBuilder.append(",threads"); //profile different threads separately
        }

        if (profileRequest.getCStack() != null) {
            stringBuilder.append(",cstack=");
            stringBuilder.append(profileRequest.getCStack().getMode());
        }

        return stringBuilder.toString();
    }

    public String createStopCommand(ProfileRequest profileRequest, String filePath, String title) {
        StringBuilder stringBuilder = new StringBuilder("stop");
        appendFileAndFormat(profileRequest, filePath, stringBuilder);

        if (profileRequest.getIncludedTraces() != null) {
            stringBuilder.append(",include=");
            stringBuilder.append(profileRequest.getIncludedTraces());
        }

        if (profileRequest.getExcludedTraces() != null) {
            stringBuilder.append(",exclude=");
            stringBuilder.append(profileRequest.getExcludedTraces());
        }

        if (title != null) {
            stringBuilder.append(",title=");
            stringBuilder.append(title);
        }

        return stringBuilder.toString();
    }

    public String createGetSupportedEventsCommand() {
        return "list";
    }

    public String createGetVersionCommand() {
        return "version";
    }

    private void appendFileAndFormat(ProfileRequest profileRequest, String filePath, StringBuilder stringBuilder) {
        stringBuilder.append(",file=");
        stringBuilder.append(filePath);

        stringBuilder.append(",");
        stringBuilder.append(profileRequest.getFormat().name().toLowerCase());
    }

    private void validate(ProfileRequest profileRequest, String filePath) {
        if (filePath == null || filePath.equals("")) {
            throw new IllegalArgumentException("File path must not be empty");
        }

        if (profileRequest.getEvents() == null || profileRequest.getEvents().isEmpty()) {
            throw new IllegalArgumentException("Events is required");
        }

        if (profileRequest.getDurationSeconds() <= 0) {
            throw new IllegalArgumentException("Profiling duration must be greater than 0");
        }

        if (profileRequest.getSamplingInterval() != null && profileRequest.getSamplingInterval() <= 0) {
            throw new IllegalArgumentException("Sampling interval must be greater than 0");
        }

        if (profileRequest.getAllocIntervalBytes() != null && profileRequest.getAllocIntervalBytes() <= 0) {
            throw new IllegalArgumentException("Alloc interval must be greater than 0");
        }

        if (profileRequest.getLockThresholdNanos() != null && profileRequest.getLockThresholdNanos() <= 0) {
            throw new IllegalArgumentException("Lock threshold must be greater than 0");
        }
    }
}
