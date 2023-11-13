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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

import io.pyroscope.labels.Pyroscope;
import io.pyroscope.okhttp3.Call;
import io.pyroscope.okhttp3.OkHttpClient;
import io.pyroscope.okhttp3.Protocol;
import io.pyroscope.okhttp3.Request;
import io.pyroscope.okhttp3.Response;
import io.pyroscope.okhttp3.internal.http.RealResponseBody;
import io.pyroscope.okio.Buffer;
import io.pyroscope.okio.RealBufferedSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;
import com.taboola.async_profiler.api.original.Format;
import com.taboola.async_profiler.utils.IOUtils;

public class PyroscopeReporterTest {

    @Mock
    private IOUtils ioUtils;
    @Mock
    private OkHttpClient httpClient;
    private PyroscopeReporter pyroscopeReporter;
    PyroscopeReporterConfig config = new PyroscopeReporterConfig();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        config = new PyroscopeReporterConfig();
        config.setAppName("app");
        config.setSpyName("spy");
        config.setPyroscopeServerAddress("http://pyroscope:4040");

        pyroscopeReporter = new PyroscopeReporter(config, ioUtils, httpClient);
    }

    @Test
    public void testReport() throws IOException {
        Set<String> events = new HashSet<>();
        events.add("itimer");
        ProfileResult profileResult = ProfileResult.builder()
                .request(ProfileRequest.builder().events(events).samplingInterval(1).samplingIntervalTimeUnit(TimeUnit.MILLISECONDS).format(Format.JFR).build())
                .resultInputStream(mock(InputStream.class))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(1))
                .build();
        Response response = createResponse(200);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(httpClient.newCall(any())).thenReturn(call);
        pyroscopeReporter.report(profileResult);
        verify(call, times(1)).execute();
    }

    public Response createResponse(int status) {
        return new Response.Builder()
                .code(status)
                .protocol(Protocol.HTTP_2)
                .message("")
                .body(new RealResponseBody("", 0, new RealBufferedSource(new Buffer())))
                .request(new Request.Builder().url("http://foo.bar").method("GET", null).build())
                .build();
    }

    @Test
    public void testReport_jfr() throws IOException {
        Set<String> events = new HashSet<>();
        events.add("itimer");
        ProfileResult profileResult = ProfileResult.builder()
                .request(ProfileRequest.builder().events(events).samplingInterval(1).samplingIntervalTimeUnit(TimeUnit.MILLISECONDS).format(Format.JFR).build())
                .resultInputStream(mock(InputStream.class))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(1))
                .build();
        Response response = createResponse(200);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(httpClient.newCall(any())).thenReturn(call);
        pyroscopeReporter.report(profileResult);
        verify(call, times(1)).execute();
    }

    @Test
    public void testReport_jfr_labels() throws IOException {
        pyroscopeReporter = new PyroscopeReporter(config, ioUtils, httpClient);

        Set<String> events = new HashSet<>();
        events.add("itimer");
        ProfileResult profileResult = ProfileResult.builder()
                .request(ProfileRequest.builder().events(events).samplingInterval(1).samplingIntervalTimeUnit(TimeUnit.MILLISECONDS).format(Format.JFR).build())
                .resultInputStream(mock(InputStream.class))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(1))
                .labels(Pyroscope.LabelsWrapper.dump())
                .build();
        Response response = createResponse(200);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(httpClient.newCall(any())).thenReturn(call);
        pyroscopeReporter.report(profileResult);
        verify(call, times(1)).execute();
    }

    @Test
    public void testReport_jfr_labels_compression() throws IOException {
        config.setCompressionLevelJFR(2);
        pyroscopeReporter = new PyroscopeReporter(config, ioUtils, httpClient);

        Set<String> events = new HashSet<>();
        events.add("itimer");
        ProfileResult profileResult = ProfileResult.builder()
                .request(ProfileRequest.builder().events(events).samplingInterval(1).samplingIntervalTimeUnit(TimeUnit.MILLISECONDS).format(Format.JFR).build())
                .resultInputStream(mock(InputStream.class))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(1))
                .labels(Pyroscope.LabelsWrapper.dump())
                .build();
        Response response = createResponse(200);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(httpClient.newCall(any())).thenReturn(call);
        pyroscopeReporter.report(profileResult);
        verify(call, times(1)).execute();
    }

    @Test
    public void testReport_whenResponseCodeIsErrorResponse_shouldThrow() throws IOException {
        Set<String> events = new HashSet<>();
        events.add("itimer");
        ProfileResult profileResult = ProfileResult.builder()
                .request(ProfileRequest.builder().events(events).samplingInterval(1).samplingIntervalTimeUnit(TimeUnit.MILLISECONDS).format(Format.JFR).build())
                .resultInputStream(mock(InputStream.class))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(1))
                .build();
        Response response = createResponse(400);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(httpClient.newCall(any())).thenReturn(call);
        assertThrows(RuntimeException.class, () -> pyroscopeReporter.report(profileResult));
    }

    @Test
    public void testReport_whenErrorToConnect_shouldThrow() throws IOException {
        Set<String> events = new HashSet<>();
        events.add("itimer");
        ProfileResult profileResult = ProfileResult.builder()
                .request(ProfileRequest.builder().events(events).samplingInterval(1).samplingIntervalTimeUnit(TimeUnit.MILLISECONDS).format(Format.JFR).build())
                .resultInputStream(mock(InputStream.class))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(1))
                .build();
        Response response = createResponse(400);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(httpClient.newCall(any())).thenThrow(new RuntimeException("some error"));
        assertThrows(RuntimeException.class, () -> pyroscopeReporter.report(profileResult));
    }
}