package com.taboola.async_profiler.api.continuous.pyroscope;

import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;
import com.taboola.async_profiler.api.original.Format;
import com.taboola.async_profiler.utils.IOUtils;
import com.taboola.async_profiler.utils.NetUtils;

public class PyroscopeReporterTest {

    @Mock
    private IOUtils ioUtils;
    @Mock
    private NetUtils netUtils;
    private PyroscopeReporterConfig config;
    private PyroscopeReporter pyroscopeReporter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        config = new PyroscopeReporterConfig();
        config.setAppName("app");
        config.setSpyName("spy");
        config.setPyroscopeServerAddress("http://pyroscope:4040");

        pyroscopeReporter = new PyroscopeReporter(config, ioUtils, netUtils);
    }

    @Test
    public void testReport() throws IOException {
        ProfileResult profileResult = new ProfileResult(new ProfileRequest(), mock(InputStream.class), LocalDateTime.now(), LocalDateTime.now().plusMinutes(1));
        profileResult.getRequest().setFormat(Format.COLLAPSED);
        Map<String, String> expectedQueryParams = new HashMap<>();
        expectedQueryParams.put("name", "app.cpu");
        expectedQueryParams.put("spyName", "spy");
        expectedQueryParams.put("aggregationType", "sum");
        expectedQueryParams.put("units", "samples");
        expectedQueryParams.put("sampleRate", "1000");
        expectedQueryParams.put("from", Long.toString(profileResult.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond()));
        expectedQueryParams.put("until", Long.toString(profileResult.getEndTime().atZone(ZoneId.systemDefault()).toEpochSecond()));
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        OutputStream outputStream = mock(OutputStream.class);

        when(netUtils.getHTTPConnection(eq(config.getPyroscopeServerAddress()),
                eq(config.getPyroscopeServerIngestPath()),
                eq(expectedQueryParams),
                eq("POST"),
                eq(config.getConnectTimeout()),
                eq(config.getReadTimeout()))).thenReturn(httpURLConnection);

        when(httpURLConnection.getOutputStream()).thenReturn(outputStream);
        when(httpURLConnection.getResponseCode()).thenReturn(200);

        pyroscopeReporter.report(profileResult);

        verify(netUtils, times(1)).getHTTPConnection(anyString(), anyString(), any(), anyString(), anyInt(), anyInt());
        verify(ioUtils, times(1)).copy(same(profileResult.getResultInputStream()), same(outputStream));
        verify(httpURLConnection, times(1)).getResponseCode();
        verify(httpURLConnection, times(1)).getOutputStream();
        verifyNoMoreInteractions(httpURLConnection);
    }

    @Test
    public void testReport_whenResponseCodeIsErrorResponse_shouldThrow() throws IOException {
        ProfileResult profileResult = new ProfileResult(new ProfileRequest(), mock(InputStream.class), LocalDateTime.now(), LocalDateTime.now().plusMinutes(1));
        profileResult.getRequest().setFormat(Format.COLLAPSED);
        Map<String, String> expectedQueryParams = new HashMap<>();
        expectedQueryParams.put("name", "app.cpu");
        expectedQueryParams.put("spyName", "spy");
        expectedQueryParams.put("aggregationType", "sum");
        expectedQueryParams.put("units", "samples");
        expectedQueryParams.put("sampleRate", "1000");
        expectedQueryParams.put("from", Long.toString(profileResult.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond()));
        expectedQueryParams.put("until", Long.toString(profileResult.getEndTime().atZone(ZoneId.systemDefault()).toEpochSecond()));
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        OutputStream outputStream = mock(OutputStream.class);

        when(netUtils.getHTTPConnection(eq(config.getPyroscopeServerAddress()),
                eq(config.getPyroscopeServerIngestPath()),
                eq(expectedQueryParams),
                eq("POST"),
                eq(config.getConnectTimeout()),
                eq(config.getReadTimeout()))).thenReturn(httpURLConnection);

        when(httpURLConnection.getOutputStream()).thenReturn(outputStream);
        when(httpURLConnection.getResponseCode()).thenReturn(400);

        assertThrows(RuntimeException.class, () -> pyroscopeReporter.report(profileResult));
    }

    @Test
    public void testReport_whenErrorToConnect_shouldThrow() throws IOException {
        ProfileResult profileResult = new ProfileResult(new ProfileRequest(), mock(InputStream.class), LocalDateTime.now(), LocalDateTime.now().plusMinutes(1));
        profileResult.getRequest().setFormat(Format.COLLAPSED);
        Map<String, String> expectedQueryParams = new HashMap<>();
        expectedQueryParams.put("name", "app.cpu");
        expectedQueryParams.put("spyName", "spy");
        expectedQueryParams.put("aggregationType", "sum");
        expectedQueryParams.put("units", "samples");
        expectedQueryParams.put("sampleRate", "1000");
        expectedQueryParams.put("from", Long.toString(profileResult.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond()));
        expectedQueryParams.put("until", Long.toString(profileResult.getEndTime().atZone(ZoneId.systemDefault()).toEpochSecond()));

        when(netUtils.getHTTPConnection(eq(config.getPyroscopeServerAddress()),
                eq(config.getPyroscopeServerIngestPath()),
                eq(expectedQueryParams),
                eq("POST"),
                eq(config.getConnectTimeout()),
                eq(config.getReadTimeout()))).thenThrow(new IOException());

        assertThrows(RuntimeException.class, () -> pyroscopeReporter.report(profileResult));
    }
}