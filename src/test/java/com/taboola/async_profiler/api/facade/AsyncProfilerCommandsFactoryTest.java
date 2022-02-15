package com.taboola.async_profiler.api.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.taboola.async_profiler.api.original.Events;
import com.taboola.async_profiler.api.original.Format;

public class AsyncProfilerCommandsFactoryTest {

    private AsyncProfilerCommandsFactory commandFactory;

    @Before
    public void setup() {
        commandFactory = new AsyncProfilerCommandsFactory();
    }

    @Test
    public void testCreateStartCommand() {
        String file = "f.ext";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setSamplingInterval(1);
        String command = commandFactory.createStartCommand(profileRequest, file);

        assertEquals("start,event=cpu,file=f.ext,flamegraph,interval=1000000", command);
    }

    @Test
    public void testCreateStartCommandWithFormat() {
        String file = "f";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setFormat(Format.COLLAPSED);
        profileRequest.setSamplingInterval(1);
        profileRequest.setSamplingIntervalTimeUnit(TimeUnit.NANOSECONDS);
        String command = commandFactory.createStartCommand(profileRequest, file);

        assertEquals("start,event=cpu,file=f,collapsed,interval=1", command);
    }

    @Test
    public void testCreateStartCommandWithMultipleEvents() {
        String file = "f";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setEvents(new LinkedHashSet<String>(){{add(Events.ALLOC);add(Events.LOCK);add(Events.CPU);}});
        profileRequest.setSamplingInterval(1);
        profileRequest.setSamplingIntervalTimeUnit(TimeUnit.NANOSECONDS);
        String command = commandFactory.createStartCommand(profileRequest, file);

        assertEquals("start,event=alloc,lock,cpu,file=f,jfr,interval=1,alloc=10000,lock=1", command);
    }

    @Test
    public void testCreateStartCommand_whenHasIncludedThreads_shouldAddFilterFlag() {
        String file = "f";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setSamplingInterval(1);
        profileRequest.setIncludedThreads("a");
        String command = commandFactory.createStartCommand(profileRequest, file);

        assertEquals("start,event=cpu,file=f,flamegraph,interval=1000000,filter", command);
    }

    @Test
    public void testCreateStartCommand_whenEventIsAlloc_intervalShouldBeTakenFromIntervalBytes() {
        String file = "f";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setEvents(new HashSet<String>(){{add(Events.ALLOC);}});
        profileRequest.setSamplingInterval(1);
        profileRequest.setAllocIntervalBytes(2);
        String command = commandFactory.createStartCommand(profileRequest, file);

        assertEquals("start,event=alloc,file=f,flamegraph,interval=1000000,alloc=2", command);
    }

    @Test
    public void testCreateStartCommand_whenValidationFails_shouldThrow() {
        ProfileRequest profileRequest = new ProfileRequest();

        ProfileRequest finalProfileRequest = profileRequest;
        assertThrows("File path must not be empty", IllegalArgumentException.class, () -> commandFactory.createStartCommand(finalProfileRequest, ""));

        profileRequest = new ProfileRequest();
        profileRequest.setEvents(null);
        ProfileRequest finalProfileRequest1 = profileRequest;
        assertThrows("Event type is required", IllegalArgumentException.class, () -> commandFactory.createStartCommand(finalProfileRequest1, "a"));

        profileRequest = new ProfileRequest();
        profileRequest.setDurationSeconds(-1);
        ProfileRequest finalProfileRequest2 = profileRequest;
        assertThrows("Profiling duration must be greater than 0", IllegalArgumentException.class, () -> commandFactory.createStartCommand(finalProfileRequest2, "a"));

        profileRequest = new ProfileRequest();
        profileRequest.setSamplingInterval(-1);
        ProfileRequest finalProfileRequest3 = profileRequest;
        assertThrows("Sampling interval must be greater than 0", IllegalArgumentException.class, () -> commandFactory.createStartCommand(finalProfileRequest3, "a"));
    }

    @Test
    public void testCreateStopCommand() {
        String file = "f";
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setIncludedTraces("included");
        profileRequest.setExcludedTraces("excluded");
        String command = commandFactory.createStopCommand(profileRequest, file, "abc");

        assertEquals("stop,file=f,flamegraph,include=included,exclude=excluded,title=abc", command);
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