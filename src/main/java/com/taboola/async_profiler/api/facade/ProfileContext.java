package com.taboola.async_profiler.api.facade;

import java.time.LocalDateTime;

import lombok.Value;

@Value
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
}
