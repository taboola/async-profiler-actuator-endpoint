package com.taboola.async_profiler.api.continuous.pyroscope;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.taboola.async_profiler.api.continuous.ProfileSnapshotsReporter;
import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;
import com.taboola.async_profiler.api.original.Events;
import com.taboola.async_profiler.api.original.Format;
import com.taboola.async_profiler.utils.IOUtils;
import com.taboola.async_profiler.utils.NetUtils;

public class PyroscopeReporter implements ProfileSnapshotsReporter {

    private final PyroscopeReporterConfig config;
    private final IOUtils ioUtils;
    private final NetUtils netUtils;

    public PyroscopeReporter(PyroscopeReporterConfig pyroscopeReporterConfig,
                             IOUtils ioUtils,
                             NetUtils netUtils) {
        this.config = pyroscopeReporterConfig;
        this.ioUtils = ioUtils;
        this.netUtils = netUtils;
    }

    @Override
    public void report(ProfileResult snapshotProfileResult) {
        validate(snapshotProfileResult);

        try {
            HttpURLConnection conn = createPyroscopeIngestRequest(snapshotProfileResult);

            int status = conn.getResponseCode();
            if (status >= 400) {
                final String responseBody = ioUtils.readToString(conn.getInputStream());
                throw new RuntimeException("Got failure response: " + responseBody);
            }

        } catch (IOException e) {
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    private void validate(ProfileResult snapshot) {
        if (snapshot.getRequest().getEvents().size() > 1) {
            throw new IllegalArgumentException("Multiple events snapshot is not supported");
        }

        if (!Format.COLLAPSED.equals(snapshot.getRequest().getFormat())) {
            throw new IllegalArgumentException("Only collapsed format is supported");
        }
    }

    private HttpURLConnection createPyroscopeIngestRequest(ProfileResult profileSnapshot) throws IOException {
        HttpURLConnection httpURLConnection = netUtils.getHTTPConnection(config.getPyroscopeServerAddress(),
                config.getPyroscopeServerIngestPath(),
                asQueryParamsMap(config, profileSnapshot),
                "POST",
                config.getConnectTimeout(),
                config.getReadTimeout());

        ioUtils.copy(profileSnapshot.getResultInputStream(), httpURLConnection.getOutputStream());

        return httpURLConnection;
    }

    private Map<String, String> asQueryParamsMap(PyroscopeReporterConfig config, ProfileResult snapshot) {
        Map<String, String> queryParams = new HashMap<>();
        String event = snapshot.getRequest().getEvents().stream().findFirst().get();

        queryParams.put("name", config.getAppName() + "." + event);
        queryParams.put("spyName", config.getSpyName());
        queryParams.put("aggregationType", "sum");

        queryParams.put("units", Events.ALLOC.equals(event) ? "objects" : "samples");
        queryParams.put("sampleRate", getIntervalInHz(snapshot.getRequest()));
        queryParams.put("from", asEpochSecondsString(snapshot.getStartTime()));
        queryParams.put("until", asEpochSecondsString(snapshot.getEndTime()));

        return queryParams;
    }

    private String asEpochSecondsString(LocalDateTime localDateTime) {
        return Long.toString(localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond());
    }

    private String getIntervalInHz(ProfileRequest profileRequest) {
        return String.valueOf(TimeUnit.SECONDS.toNanos(1) / profileRequest.getSamplingIntervalTimeUnit().toNanos(profileRequest.getSamplingInterval()));
    }
}
