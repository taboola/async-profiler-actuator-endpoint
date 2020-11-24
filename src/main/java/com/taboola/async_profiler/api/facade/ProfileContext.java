package com.taboola.async_profiler.api.facade;

import java.time.LocalDateTime;
import java.util.Objects;

public class ProfileContext {
    private final ProfileRequest profileRequest;
    private final LocalDateTime startTime;
    private final String tmpFilePath;
    private final Thread requestThread;

    public ProfileContext(ProfileRequest profileRequest,
                          LocalDateTime startTime,
                          String tmpFilePath,
                          Thread requestThread) {
        this.profileRequest = profileRequest;
        this.startTime = startTime;
        this.tmpFilePath = tmpFilePath;
        this.requestThread = requestThread;
    }

    public ProfileRequest getProfileRequest() {
        return profileRequest;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public String getTmpFilePath() {
        return tmpFilePath;
    }

    public Thread getRequestThread() {
        return requestThread;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileContext that = (ProfileContext) o;
        return Objects.equals(profileRequest, that.profileRequest) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(tmpFilePath, that.tmpFilePath) &&
                Objects.equals(requestThread, that.requestThread);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileRequest, startTime, tmpFilePath, requestThread);
    }

    @Override
    public String toString() {
        return "ProfileContext{" +
                "profileRequest=" + profileRequest +
                ", startTime=" + startTime +
                ", tmpFilePath='" + tmpFilePath + '\'' +
                ", requestThread=" + requestThread +
                '}';
    }
}
