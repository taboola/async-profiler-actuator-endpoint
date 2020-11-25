package com.taboola.async_profiler.spring;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.ProfileRequest;

public class AsyncProfilerEndPointTest {

    @Mock
    private AsyncProfilerFacade asyncProfilerFacade;

    private AsyncProfilerEndPoint asyncProfilerEndPoint;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        asyncProfilerEndPoint = new AsyncProfilerEndPoint(asyncProfilerFacade);
    }

    @Test
    public void testProfile() {
        ProfileRequest profileRequest = mock(ProfileRequest.class);
        OutputStream outputStream = mock(OutputStream.class);

        asyncProfilerEndPoint.profile(profileRequest, outputStream);
        verify(asyncProfilerFacade, times(1)).profile(same(profileRequest), same(outputStream));
    }

    @Test
    public void testStop() {
        OutputStream outputStream = mock(OutputStream.class);

        asyncProfilerEndPoint.stop(outputStream);
        verify(asyncProfilerFacade, times(1)).stop(same(outputStream));
    }

    @Test
    public void testGetSupportedEvents() {
        when(asyncProfilerFacade.getSupportedEvents()).thenReturn("cpu");
        assertEquals("cpu", asyncProfilerEndPoint.getSupportedEvents());
    }

    @Test
    public void testGetVersion() {
        when(asyncProfilerFacade.getProfilerVersion()).thenReturn("1");
        assertEquals("1", asyncProfilerEndPoint.getVersion());
    }

}