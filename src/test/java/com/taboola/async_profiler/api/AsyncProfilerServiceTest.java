package com.taboola.async_profiler.api;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.taboola.async_profiler.api.continuous.ContinuousProfilingSnapshotRequest;
import com.taboola.async_profiler.api.continuous.ProfileResultsReporter;
import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;
import com.taboola.async_profiler.api.serviceconfig.AsyncProfilerServiceConfigurations;
import com.taboola.async_profiler.utils.RecurringRunnable;
import com.taboola.async_profiler.utils.ThreadUtils;

public class AsyncProfilerServiceTest {

    @Mock
    private AsyncProfilerFacade asyncProfilerFacade;
    @Mock
    private ProfileResultsReporter profileResultsReporter;
    @Mock
    private ExecutorService continuousProfilingExecutorService;
    @Mock
    private ExecutorService snapshotsReporterExecutorService;
    @Mock
    private ThreadUtils threadUtils;
    private AsyncProfilerServiceConfigurations config;
    private AsyncProfilerService asyncProfilerService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        config = new AsyncProfilerServiceConfigurations();
        asyncProfilerService = new AsyncProfilerService(asyncProfilerFacade, profileResultsReporter, continuousProfilingExecutorService, snapshotsReporterExecutorService, config, threadUtils);
    }

    @Test
    public void testProfile() {
        ProfileRequest profileRequest = new ProfileRequest();
        ProfileResult profileResult = new ProfileResult(profileRequest, null, null, null);

        when(asyncProfilerFacade.profile(eq(profileRequest))).thenReturn(profileResult);

        ProfileResult result = asyncProfilerService.profile(profileRequest);

        assertSame(profileResult, result);
        verify(asyncProfilerFacade, times(1)).profile(same(profileRequest));
        verifyNoMoreInteractions(asyncProfilerFacade);
    }

    @Test
    public void testStop() {
        ProfileRequest profileRequest = new ProfileRequest();
        ProfileResult profileResult = new ProfileResult(profileRequest, null, null, null);

        when(asyncProfilerFacade.stop()).thenReturn(profileResult);

        ProfileResult result = asyncProfilerService.stop();

        assertSame(profileResult, result);
        verify(asyncProfilerFacade, times(1)).stop();
        verifyNoMoreInteractions(asyncProfilerFacade);
    }

    @Test
    public void testStartAndStopContinuousProfiling() throws Exception {
        ContinuousProfilingSnapshotRequest profileRequest = new ContinuousProfilingSnapshotRequest();
        ProfileResult profileResult = new ProfileResult(profileRequest, mock(InputStream.class), null, null);

        when(asyncProfilerFacade.profile(eq(profileRequest))).thenReturn(profileResult);

        asyncProfilerService.startContinuousProfiling(profileRequest);

        ArgumentCaptor<RecurringRunnable> recurringRunnableArgumentCaptor = ArgumentCaptor.forClass(RecurringRunnable.class);
        verify(continuousProfilingExecutorService, times(1)).submit(recurringRunnableArgumentCaptor.capture());
        RecurringRunnable continuousProfilingTask = recurringRunnableArgumentCaptor.getValue();
        Runnable innerTask = continuousProfilingTask.getBaseRunnable();

        verifyZeroInteractions(asyncProfilerFacade);

        innerTask.run();

        verify(asyncProfilerFacade, times(1)).profile(eq(profileRequest));
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(snapshotsReporterExecutorService, times(1)).submit(runnableArgumentCaptor.capture());
        Runnable reportingTask = runnableArgumentCaptor.getValue();

        reportingTask.run();

        verify(profileResultsReporter, times(1)).report(eq(profileResult));
        verify(profileResult.getResultInputStream(), atLeast(1)).close();
        verifyNoMoreInteractions(asyncProfilerFacade);

        asyncProfilerService.stopContinuousProfiling();
        assertTrue(continuousProfilingTask.isCancelled());
        verify(asyncProfilerFacade, times(1)).stop();
    }

    @Test
    public void testStartContinuousProfilingOnInit() throws Exception {
        ProfileRequest profileRequest = new ContinuousProfilingSnapshotRequest();
        ProfileResult profileResult = new ProfileResult(profileRequest, mock(InputStream.class), null, null);
        when(asyncProfilerFacade.profile(eq(profileRequest))).thenReturn(profileResult);
        config.getContinuousProfiling().setStartOnInit(true);

        asyncProfilerService = new AsyncProfilerService(asyncProfilerFacade, profileResultsReporter, continuousProfilingExecutorService, snapshotsReporterExecutorService, config, threadUtils);

        ArgumentCaptor<RecurringRunnable> recurringRunnableArgumentCaptor = ArgumentCaptor.forClass(RecurringRunnable.class);
        verify(continuousProfilingExecutorService, times(1)).submit(recurringRunnableArgumentCaptor.capture());
        RecurringRunnable continuousProfilingTask = recurringRunnableArgumentCaptor.getValue();
        Runnable innerTask = continuousProfilingTask.getBaseRunnable();

        verifyZeroInteractions(asyncProfilerFacade);

        innerTask.run();

        verify(asyncProfilerFacade, times(1)).profile(eq(profileRequest));
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(snapshotsReporterExecutorService, times(1)).submit(runnableArgumentCaptor.capture());
        Runnable reportingTask = runnableArgumentCaptor.getValue();

        reportingTask.run();

        verify(profileResultsReporter, times(1)).report(eq(profileResult));
        verify(profileResult.getResultInputStream(), atLeast(1)).close();
        verifyNoMoreInteractions(asyncProfilerFacade);
    }

    @Test
    public void testStartContinuousProfiling_whenAlreadyStarted_shouldThrow() throws Exception {
        ContinuousProfilingSnapshotRequest profileRequest = new ContinuousProfilingSnapshotRequest();
        ProfileResult profileResult = new ProfileResult(profileRequest, mock(InputStream.class), null, null);

        when(asyncProfilerFacade.profile(eq(profileRequest))).thenReturn(profileResult);

        asyncProfilerService.startContinuousProfiling(profileRequest);

        assertThrows(IllegalStateException.class, () -> asyncProfilerService.startContinuousProfiling(profileRequest));
    }

    @Test
    public void testStopContinuousProfiling_whenNothingToStop_shouldThrow() {
        assertThrows(IllegalStateException.class, () -> asyncProfilerService.stopContinuousProfiling());
    }

    @Test
    public void testGetSupportedEvents() {
        when(asyncProfilerFacade.getSupportedEvents()).thenReturn("events");

        String result = asyncProfilerService.getSupportedEvents();

        assertEquals("events", result);
    }

    @Test
    public void testGetProfilerVersion() {
        when(asyncProfilerFacade.getProfilerVersion()).thenReturn("10");

        String result = asyncProfilerService.getProfilerVersion();

        assertEquals("10", result);
    }

}