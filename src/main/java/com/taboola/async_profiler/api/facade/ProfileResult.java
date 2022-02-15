package com.taboola.async_profiler.api.facade;

import lombok.Value;

import java.io.InputStream;

import com.taboola.async_profiler.api.original.Format;

@Value
public class ProfileResult {
    InputStream resultInputStream;
    Format format;
}
