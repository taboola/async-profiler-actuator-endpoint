package com.taboola.async_profiler.api.facade;

import java.io.InputStream;
import java.time.LocalDateTime;

import lombok.Value;

@Value
public class ProfileResult implements AutoCloseable {
    ProfileRequest request;

    InputStream resultInputStream;
    LocalDateTime startTime;
    LocalDateTime endTime;

    @Override
    public void close() throws Exception {
        if (resultInputStream != null) {
            resultInputStream.close();
        }
    }
}
