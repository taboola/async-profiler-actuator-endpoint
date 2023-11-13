package com.taboola.async_profiler.api.facade;

import java.io.InputStream;
import java.time.LocalDateTime;

import io.pyroscope.labels.pb.JfrLabels;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProfileResult implements AutoCloseable {
    ProfileRequest request;
    InputStream resultInputStream;
    LocalDateTime startTime;
    LocalDateTime endTime;
    JfrLabels.Snapshot labels;

    @Override
    public void close() throws Exception {
        if (resultInputStream != null) {
            resultInputStream.close();
        }
    }
}
