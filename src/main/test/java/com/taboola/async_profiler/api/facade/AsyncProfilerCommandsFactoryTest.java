package com.taboola.async_profiler.api.facade;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AsyncProfilerCommandsFactoryTest {

    private AsyncProfilerCommandsFactory commandFactory;

    @Before
    public void setup() {
        commandFactory = new AsyncProfilerCommandsFactory();
    }

    @Test
    public void testCreateStartCommand() {
        String file = "f";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setEventType("cpu");
        profileRequest.setFormat("flamegraph");
        profileRequest.setSamplingIntervalMs(1);
        profileRequest.setFrameBufferSize(10);
        String command = commandFactory.createStartCommand(profileRequest, file);

        assertEquals("start,event=cpu,file=f,flamegraph,interval=1000000,framebuf=10", command);
    }

    @Test
    public void testCreateStartCommand_whenHasIncludedThreads_shouldAddFilterFlag() {
        String file = "f";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setEventType("cpu");
        profileRequest.setFormat("flamegraph");
        profileRequest.setSamplingIntervalMs(1);
        profileRequest.setFrameBufferSize(10);
        profileRequest.setIncludedThreads("a");
        String command = commandFactory.createStartCommand(profileRequest, file);

        assertEquals("start,event=cpu,file=f,flamegraph,interval=1000000,framebuf=10,filter", command);
    }

    @Test
    public void testCreateStartCommand_whenEventIsAlloc_intervalShouldBeTakenFromIntervalBytes() {
        String file = "f";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setEventType("alloc");
        profileRequest.setFormat("flamegraph");
        profileRequest.setSamplingIntervalMs(1);
        profileRequest.setSamplingIntervalBytes(2);
        profileRequest.setFrameBufferSize(10);
        String command = commandFactory.createStartCommand(profileRequest, file);

        assertEquals("start,event=alloc,file=f,flamegraph,interval=2,framebuf=10", command);
    }

    @Test
    public void testCreateStartCommand_whenValidationFails_shouldThrow() {
        ProfileRequest profileRequest = new ProfileRequest();

        ProfileRequest finalProfileRequest = profileRequest;
        assertThrows("File path must not be empty", IllegalArgumentException.class, () -> commandFactory.createStartCommand(finalProfileRequest, ""));

        profileRequest = new ProfileRequest();
        profileRequest.setEventType("");
        ProfileRequest finalProfileRequest1 = profileRequest;
        assertThrows("Event type is required", IllegalArgumentException.class, () -> commandFactory.createStartCommand(finalProfileRequest1, "a"));

        profileRequest = new ProfileRequest();
        profileRequest.setDurationSeconds(-1);
        ProfileRequest finalProfileRequest2 = profileRequest;
        assertThrows("Profiling duration must be greater than 0", IllegalArgumentException.class, () -> commandFactory.createStartCommand(finalProfileRequest2, "a"));

        profileRequest = new ProfileRequest();
        profileRequest.setSamplingIntervalMs(-1);
        ProfileRequest finalProfileRequest3 = profileRequest;
        assertThrows("Sampling interval must be greater than 0", IllegalArgumentException.class, () -> commandFactory.createStartCommand(finalProfileRequest3, "a"));
    }

    @Test
    public void testCreateStopCommand() {
        String file = "f";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setFormat("flame");
        profileRequest.setIncludedTraces("included");
        profileRequest.setExcludedTraces("excluded");
        String command = commandFactory.createStopCommand(profileRequest, file, "abc");

        assertEquals("stop,file=f,flame,include=included,exclude=excluded,title=abc", command);
    }

    @Test
    public void testCreateGetEventsCommand() {
        assertEquals("list", commandFactory.createGetSupportedEventsCommand());
    }

    @Test
    public void testCreateGetVersionCommand() {
        assertEquals("version", commandFactory.createGetVersionCommand());
    }
}