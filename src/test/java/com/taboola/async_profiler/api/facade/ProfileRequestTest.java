package com.taboola.async_profiler.api.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.taboola.async_profiler.api.original.Events;
import com.taboola.async_profiler.api.original.Format;

public class ProfileRequestTest {

    @Test
    public void testDefaultValues() {
        ProfileRequest profileRequest = new ProfileRequest();

        assertEquals(60, profileRequest.getDurationSeconds());
        assertEquals(1, profileRequest.getSamplingInterval().intValue());
        assertEquals(TimeUnit.MILLISECONDS, profileRequest.getSamplingIntervalTimeUnit());
        assertEquals(10_000, profileRequest.getAllocIntervalBytes().intValue());
        assertEquals(1, profileRequest.getLockThresholdNanos().intValue());
        assertEquals(new HashSet<String>(){{add(Events.CPU);}}, profileRequest.getEvents());
        assertEquals(Format.FLAMEGRAPH, profileRequest.getFormat());
        assertNull(profileRequest.getIncludedTraces());
        assertNull(profileRequest.getExcludedTraces());
        assertNull(profileRequest.getIncludedThreads());
    }

    @Test
    public void testGetFormat() {
        ProfileRequest profileRequest = new ProfileRequest();
        assertEquals(Format.FLAMEGRAPH, profileRequest.getFormat());

        profileRequest.setFormat(Format.COLLAPSED);
        assertEquals(Format.COLLAPSED, profileRequest.getFormat());

        profileRequest.setEvents(new HashSet<String>(){{add(Events.CPU);add(Events.ALLOC);}});
        assertEquals(Format.JFR, profileRequest.getFormat());
    }
}
