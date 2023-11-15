package com.taboola.async_profiler.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.taboola.async_profiler.api.AsyncProfilerService;
import com.taboola.async_profiler.api.continuous.ContinuousProfilingSnapshotRequest;
import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;
import com.taboola.async_profiler.api.original.Format;

public class AsyncProfilerEndpointTest {

    @Mock
    private AsyncProfilerService asyncProfilerService;

    private AsyncProfilerEndpoint asyncProfilerEndPoint;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        asyncProfilerEndPoint = new AsyncProfilerEndpoint(asyncProfilerService, false);
    }

    @Test
    public void testProfile() throws IOException {
        ProfileRequest profileRequest = mock(ProfileRequest.class);
        InputStream inputStream = mock(InputStream.class);
        ProfileResult profileResult = ProfileResult.builder().request(profileRequest).resultInputStream(inputStream).startTime(LocalDateTime.now()).endTime(LocalDateTime.now()).build();
        when(profileRequest.getFormat()).thenReturn(Format.FLAMEGRAPH);
        when(asyncProfilerService.profile(same(profileRequest))).thenReturn(profileResult);

        ResponseEntity result = asyncProfilerEndPoint.profile(profileRequest);

        assertSame(inputStream, ((InputStreamSource) result.getBody()).getInputStream());
        assertEquals(0, result.getHeaders().size());
        verify(asyncProfilerService, times(1)).profile(same(profileRequest));
    }

    @Test
    public void testProfileWithBinaryFormatResult() throws IOException {
        ProfileRequest profileRequest = mock(ProfileRequest.class);
        InputStream inputStream = mock(InputStream.class);
        ProfileResult profileResult = ProfileResult.builder().request(profileRequest).resultInputStream(inputStream).startTime(LocalDateTime.now()).endTime(LocalDateTime.now()).build();
        when(profileRequest.getFormat()).thenReturn(Format.JFR);
        when(asyncProfilerService.profile(same(profileRequest))).thenReturn(profileResult);

        ResponseEntity result = asyncProfilerEndPoint.profile(profileRequest);

        assertSame(inputStream, ((InputStreamSource) result.getBody()).getInputStream());
        assertEquals(1, result.getHeaders().size());
        assertEquals(Collections.singletonList("attachment; filename=profile.jfr"), result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION));
        verify(asyncProfilerService, times(1)).profile(same(profileRequest));
    }

    @Test
    public void testStop() throws IOException {
        ProfileRequest profileRequest = mock(ProfileRequest.class);
        InputStream inputStream = mock(InputStream.class);
        ProfileResult profileResult = ProfileResult.builder().request(profileRequest).resultInputStream(inputStream).startTime(LocalDateTime.now()).endTime(LocalDateTime.now()).build();
        when(profileRequest.getFormat()).thenReturn(Format.FLAMEGRAPH);
        when(asyncProfilerService.stop()).thenReturn(profileResult);

        ResponseEntity result = asyncProfilerEndPoint.stop();

        assertSame(inputStream, ((InputStreamSource) result.getBody()).getInputStream());
        assertEquals(0, result.getHeaders().size());
        verify(asyncProfilerService, times(1)).stop();
    }

    @Test
    public void testStartContinuousProfiling() {
        ContinuousProfilingSnapshotRequest profileRequest = new ContinuousProfilingSnapshotRequest();

        asyncProfilerEndPoint.startContinuous(profileRequest);

        verify(asyncProfilerService, times(1)).startContinuousProfiling(same(profileRequest));
    }

    @Test
    public void testStopContinuousProfiling() {
        asyncProfilerEndPoint.stopContinuous();

        verify(asyncProfilerService, times(1)).stopContinuousProfiling();
    }

    @Test
    public void testGetSupportedEvents() {
        when(asyncProfilerService.getSupportedEvents()).thenReturn("cpu");
        assertEquals("cpu", asyncProfilerEndPoint.getSupportedEvents());
    }

    @Test
    public void testGetVersion() {
        when(asyncProfilerService.getProfilerVersion()).thenReturn("1");
        assertEquals("1", asyncProfilerEndPoint.getVersion());
    }

    @Test
    public void testIsSensitive() {
        assertFalse(new AsyncProfilerEndpoint(asyncProfilerService, false).isSensitive());
        assertTrue(new AsyncProfilerEndpoint(asyncProfilerService, true).isSensitive());
    }
}