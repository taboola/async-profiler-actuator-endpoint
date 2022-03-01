package com.taboola.async_profiler.api.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.taboola.async_profiler.api.facade.profiler.AsyncProfiler;
import com.taboola.async_profiler.api.original.Format;
import com.taboola.async_profiler.utils.IOUtils;
import com.taboola.async_profiler.utils.ThreadUtils;

public class AsyncProfilerFacadeTest {

    @Mock
    private AsyncProfiler asyncProfiler;
    @Mock
    private AsyncProfilerCommandsFactory commandsFactory;
    @Mock
    private ThreadUtils threadUtils;
    @Mock
    private IOUtils ioUtils;

    private String tmpFile;
    private AsyncProfilerFacade asyncProfilerFacade;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        tmpFile = "tmpFile";
        this.asyncProfilerFacade = new AsyncProfilerFacade(asyncProfiler, tmpFile, commandsFactory, threadUtils, ioUtils);
    }

    @Test
    public void testProfile() throws IOException, InterruptedException {
        ProfileRequest profileRequest = new ProfileRequest();
        InputStream inputStreamMock = mock(InputStream.class);

        when(ioUtils.createTempFile(eq("tmpFile"), eq(".tmp"))).thenReturn("tmpFilePath");
        when(commandsFactory.createStartCommand(eq(profileRequest), eq("tmpFilePath"))).thenReturn("start");
        when(commandsFactory.createStopCommand(eq(profileRequest), eq("tmpFilePath"), startsWith("[cpu] Flame Graph"))).thenReturn("stop");
        when(ioUtils.getDisposableFileInputStream(eq("tmpFilePath"))).thenReturn(inputStreamMock);

        ProfileResult profileResult = asyncProfilerFacade.profile(profileRequest);

        assertSame(inputStreamMock, profileResult.getResultInputStream());
        assertEquals(Format.FLAMEGRAPH, profileResult.getRequest().getFormat());

        verify(ioUtils, times(1)).createTempFile(eq("tmpFile"), eq(".tmp"));
        verify(asyncProfiler, times(1)).execute(eq("start"));
        verify(threadUtils, times(1)).sleep(eq((long)profileRequest.getDurationSeconds()), eq(TimeUnit.SECONDS));
        verify(asyncProfiler, times(1)).execute(eq("stop"));
    }

    @Test
    public void testProfile_whenContainIncludedThreads_shouldFindAndAddThemToTheProfiler() throws IOException, InterruptedException {
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setIncludedThreads("qtp");
        List<Thread> profiledThreads = new ArrayList<>();
        profiledThreads.add(mock(Thread.class));
        profiledThreads.get(0).setName("qtp-123");
        profiledThreads.add(mock(Thread.class));
        profiledThreads.get(1).setName("qtp-124");
        InputStream inputStreamMock = mock(InputStream.class);

        when(ioUtils.createTempFile(eq("tmpFile"), eq(".tmp"))).thenReturn("tmpFilePath");
        when(commandsFactory.createStartCommand(eq(profileRequest), eq("tmpFilePath"))).thenReturn("start");
        when(threadUtils.getAllThreads(any())).thenReturn(profiledThreads);
        when(commandsFactory.createStopCommand(eq(profileRequest), eq("tmpFilePath"), startsWith("[cpu] Flame Graph"))).thenReturn("stop");
        when(ioUtils.getDisposableFileInputStream(eq("tmpFilePath"))).thenReturn(inputStreamMock);

        ProfileResult profileResult = asyncProfilerFacade.profile(profileRequest);

        assertSame(inputStreamMock, profileResult.getResultInputStream());
        assertEquals(Format.FLAMEGRAPH, profileResult.getRequest().getFormat());

        verify(ioUtils, times(1)).createTempFile(eq("tmpFile"), eq(".tmp"));
        verify(asyncProfiler, times(1)).execute(eq("start"));
        verify(asyncProfiler, times(1)).addThread(same(profiledThreads.get(0)));
        verify(asyncProfiler, times(1)).addThread(same(profiledThreads.get(1)));
        verify(threadUtils, times(1)).sleep(eq((long)profileRequest.getDurationSeconds()), eq(TimeUnit.SECONDS));
        verify(asyncProfiler, times(1)).execute(eq("stop"));
    }

    @Test
    public void testProfile_whenDidNotStartSuccessfully_shouldThrow() throws IOException {
        ProfileRequest profileRequest = new ProfileRequest();

        when(ioUtils.createTempFile(eq("tmpFile"), eq(".tmp"))).thenReturn("tmpFilePath");
        when(commandsFactory.createStartCommand(eq(profileRequest), eq("tmpFilePath"))).thenReturn("start");
        when(asyncProfiler.execute(eq("start"))).thenThrow(new IllegalStateException("failure"));
        when(commandsFactory.createStopCommand(eq(profileRequest), eq("tmpFilePath"), startsWith("CPU Flame Graph"))).thenReturn("stop");

        assertThrows(RuntimeException.class, () -> asyncProfilerFacade.profile(profileRequest));
        verify(ioUtils, times(1)).safeDeleteIfExists(eq("tmpFilePath"));
    }

    @Test
    public void testProfile_whenDidNotStopSuccessfully_shouldThrow() throws IOException {
        ProfileRequest profileRequest = new ProfileRequest();

        when(ioUtils.createTempFile(eq("tmpFile"), eq(".tmp"))).thenReturn("tmpFilePath");
        when(commandsFactory.createStopCommand(eq(profileRequest), eq("tmpFilePath"), startsWith("[cpu] Flame Graph"))).thenReturn("stop");
        when(asyncProfiler.execute(eq("stop"))).thenThrow(new IllegalStateException("failure"));

        assertThrows(RuntimeException.class, () -> asyncProfilerFacade.profile(profileRequest));
        verify(ioUtils, times(1)).safeDeleteIfExists(eq("tmpFilePath"));
    }

    @Test
    public void testProfile_whenProfilerThrows_shouldThrow() throws IOException {
        ProfileRequest profileRequest = new ProfileRequest();

        when(ioUtils.createTempFile(eq("tmpFile"), eq(".tmp"))).thenReturn("tmpFilePath");
        when(asyncProfiler.execute(any())).thenThrow(new RuntimeException("failure in profiler"));

        assertThrows(RuntimeException.class, () -> asyncProfilerFacade.profile(profileRequest));
        verify(ioUtils, times(1)).safeDeleteIfExists(eq("tmpFilePath"));
    }

    @Test
    public void testStop() throws IOException {
        AsyncProfilerFacade asyncProfilerFacadeSpy = spy(asyncProfilerFacade);
        Thread originalRequestThread = mock(Thread.class);
        ProfileContext originalProfileContext = new ProfileContext(new ProfileRequest(), LocalDateTime.now(), "tmpFilePath", originalRequestThread);
        InputStream inputStreamMock = mock(InputStream.class);

        when(asyncProfilerFacadeSpy.getCurrentProfileRequestContext()).thenReturn(originalProfileContext);
        when(commandsFactory.createStopCommand(eq(originalProfileContext.getProfileRequest()), eq(originalProfileContext.getTmpFilePath()), startsWith("[cpu] Flame Graph"))).thenReturn("stop");
        when(ioUtils.getDisposableFileInputStream(eq("tmpFilePath"))).thenReturn(inputStreamMock);

        ProfileResult profileResult = asyncProfilerFacadeSpy.stop();

        assertSame(inputStreamMock, profileResult.getResultInputStream());
        assertEquals(Format.FLAMEGRAPH, profileResult.getRequest().getFormat());

        verify(asyncProfiler, times(1)).execute(eq("stop"));
        verify(originalRequestThread, times(1)).interrupt();
    }

    @Test
    public void testStop_whenNoActiveRequest_shouldThrow() throws IOException {
        AsyncProfilerFacade asyncProfilerFacadeSpy = spy(asyncProfilerFacade);
        Thread originalRequestThread = mock(Thread.class);
        ProfileContext originalProfileContext = new ProfileContext(new ProfileRequest(), LocalDateTime.now(), "tmpFilePath", originalRequestThread);

        when(asyncProfilerFacadeSpy.getCurrentProfileRequestContext()).thenReturn(null);
        when(commandsFactory.createStopCommand(eq(originalProfileContext.getProfileRequest()), eq(originalProfileContext.getTmpFilePath()), startsWith("CPU Flame Graph"))).thenReturn("stop");
        when(asyncProfiler.execute(eq("stop"))).thenReturn("OKstop");

        assertThrows(IllegalStateException.class, asyncProfilerFacadeSpy::stop);
    }

    @Test
    public void testGetSupportedEvents() throws IOException {
        when(commandsFactory.createGetSupportedEventsCommand()).thenReturn("e");
        when(asyncProfiler.execute("e")).thenReturn("cpu");
        assertEquals("cpu", asyncProfilerFacade.getSupportedEvents());
    }

    @Test
    public void testGetVersion() throws IOException {
        when(commandsFactory.createGetVersionCommand()).thenReturn("v");
        when(asyncProfiler.execute("v")).thenReturn("1");
        assertEquals("1", asyncProfilerFacade.getProfilerVersion());
    }
}