package com.taboola.async_profiler.api.facade;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProfileRequestTest {

    @Test
    public void testDefaultValues() {
        ProfileRequest profileRequest = new ProfileRequest();

        assertEquals(60, profileRequest.getDurationSeconds());
        assertEquals(5_000_000, profileRequest.getFrameBufferSize());
        assertEquals(1, profileRequest.getSamplingIntervalMs().intValue());
        assertEquals(10_000_000, profileRequest.getSamplingIntervalBytes().intValue());
        assertEquals("cpu", profileRequest.getEventType());
        assertEquals("svg", profileRequest.getFormat());
        assertNull(profileRequest.getIncludedTraces());
        assertNull(profileRequest.getExcludedTraces());
        assertNull(profileRequest.getIncludedThreads());
    }

    @Test
    public void testHasIncludedThreads() {
        ProfileRequest profileRequest = new ProfileRequest();

        profileRequest.setIncludedThreads(null);
        assertFalse(profileRequest.hasIncludedThreads());

        profileRequest.setIncludedThreads("");
        assertFalse(profileRequest.hasIncludedThreads());

        profileRequest.setIncludedThreads("a");
        assertTrue(profileRequest.hasIncludedThreads());
    }

    @Test
    public void testIsFlameGraphRequest() {
        ProfileRequest profileRequest = new ProfileRequest();

        assertTrue(profileRequest.isFlameGraphRequest());

        profileRequest.setFormat("svg");
        assertTrue(profileRequest.isFlameGraphRequest());

        profileRequest.setFormat("flamegraph");
        assertTrue(profileRequest.isFlameGraphRequest());

        profileRequest.setFormat("a");
        assertFalse(profileRequest.isFlameGraphRequest());
    }
}
