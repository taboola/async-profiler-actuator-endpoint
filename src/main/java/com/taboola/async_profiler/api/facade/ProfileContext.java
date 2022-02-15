package com.taboola.async_profiler.api.facade;

import java.time.LocalDateTime;

import lombok.Value;

@Value
public class ProfileContext {
    ProfileRequest profileRequest;
    LocalDateTime startTime;
    String tmpFilePath;
    Thread requestThread;
}
