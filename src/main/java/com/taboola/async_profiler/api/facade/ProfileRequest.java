package com.taboola.async_profiler.api.facade;

import java.util.Objects;

import com.taboola.async_profiler.api.original.Events;

public class ProfileRequest {

    private int durationSeconds = 60;//profiling duration
    private int frameBufferSize = 5_000_000;
    private Integer samplingIntervalMs = 10;//10ms default, relevant only for non alloc events.
    private Integer samplingIntervalBytes = 10_000_000;//relevant only for alloc event.
    private String eventType = Events.CPU;
    private String includedThreads;
    private String includedTraces;
    private String excludedTraces;

    public ProfileRequest() {
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getSamplingIntervalMs() {
        return samplingIntervalMs;
    }

    public void setSamplingIntervalMs(Integer samplingIntervalMs) {
        this.samplingIntervalMs = samplingIntervalMs;
    }

    public Integer getSamplingIntervalBytes() {
        return samplingIntervalBytes;
    }

    public void setSamplingIntervalBytes(Integer samplingIntervalBytes) {
        this.samplingIntervalBytes = samplingIntervalBytes;
    }

    public int getFrameBufferSize() {
        return frameBufferSize;
    }

    public void setFrameBufferSize(int frameBufferSize) {
        this.frameBufferSize = frameBufferSize;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getIncludedThreads() {
        return includedThreads;
    }

    public void setIncludedThreads(String includedThreads) {
        this.includedThreads = includedThreads;
    }

    public String getIncludedTraces() {
        return includedTraces;
    }

    public void setIncludedTraces(String includedTraces) {
        this.includedTraces = includedTraces;
    }

    public String getExcludedTraces() {
        return excludedTraces;
    }

    public void setExcludedTraces(String excludedTraces) {
        this.excludedTraces = excludedTraces;
    }

    public boolean hasIncludedThreads() {
        return includedThreads != null && !includedThreads.equals("");
    }

    @Override
    public String toString() {
        return "ProfileRequest{" +
                "durationSeconds=" + durationSeconds +
                ", samplingIntervalMs=" + samplingIntervalMs +
                ", samplingIntervalBytes=" + samplingIntervalBytes +
                ", frameBufferSize=" + frameBufferSize +
                ", eventType='" + eventType + '\'' +
                ", includedThreads='" + includedThreads + '\'' +
                ", includedTraces='" + includedTraces + '\'' +
                ", excludedTraces='" + excludedTraces + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileRequest that = (ProfileRequest) o;
        return durationSeconds == that.durationSeconds &&
                frameBufferSize == that.frameBufferSize &&
                Objects.equals(samplingIntervalMs, that.samplingIntervalMs) &&
                Objects.equals(samplingIntervalBytes, that.samplingIntervalBytes) &&
                Objects.equals(eventType, that.eventType) &&
                Objects.equals(includedThreads, that.includedThreads) &&
                Objects.equals(includedTraces, that.includedTraces) &&
                Objects.equals(excludedTraces, that.excludedTraces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationSeconds, samplingIntervalMs, samplingIntervalBytes, frameBufferSize, eventType, includedThreads, includedTraces, excludedTraces);
    }
}