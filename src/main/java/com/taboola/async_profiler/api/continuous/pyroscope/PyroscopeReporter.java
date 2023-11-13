package com.taboola.async_profiler.api.continuous.pyroscope;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.Deflater;

import com.taboola.async_profiler.api.continuous.ProfileResultsReporter;
import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;
import com.taboola.async_profiler.api.original.Events;
import com.taboola.async_profiler.api.original.Format;
import com.taboola.async_profiler.utils.IOUtils;
import io.pyroscope.javaagent.util.zip.GzipSink;
import io.pyroscope.okhttp3.HttpUrl;
import io.pyroscope.okhttp3.MediaType;
import io.pyroscope.okhttp3.MultipartBody;
import io.pyroscope.okhttp3.OkHttpClient;
import io.pyroscope.okhttp3.Request;
import io.pyroscope.okhttp3.RequestBody;
import io.pyroscope.okhttp3.Response;
import io.pyroscope.okhttp3.ResponseBody;
import io.pyroscope.okio.BufferedSink;

public class PyroscopeReporter implements ProfileResultsReporter {

    private final PyroscopeReporterConfig config;
    private final IOUtils ioUtils;
    private final OkHttpClient httpClient;

    public PyroscopeReporter(PyroscopeReporterConfig pyroscopeReporterConfig,
                             IOUtils ioUtils,
                             OkHttpClient httpClient) {
        this.config = pyroscopeReporterConfig;
        this.ioUtils = ioUtils;
        this.httpClient = httpClient;
    }

    @Override
    public void report(ProfileResult profileResult) {
        validate(profileResult);
        try (Response response = uploadProfileResult(profileResult, config)) {
            int status = response.code();
            if (status >= 400) {
                ResponseBody body = response.body();
                final String responseBody = body == null ? "" : body.string();
                throw new RuntimeException("Got failure response: " + responseBody);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    private void validate(ProfileResult profileResult) {
        if (Format.COLLAPSED.equals(profileResult.getRequest().getFormat())
                && profileResult.getRequest().getEvents().contains(Events.ALLOC)) {
            // https://github.com/grafana/pyroscope/pull/2362
            throw new IllegalArgumentException("Collapsed format does not support Alloc event");
        }
    }

    private String getName(final ProfileResult profileResult, final PyroscopeReporterConfig config) {
        ProfileRequest request = profileResult.getRequest();
        List<String> events = new ArrayList<>(request.getEvents());
        Collections.sort(events);
        return String.format("%s.%s", config.getAppName(), String.join(".", events));
    }
    private HttpUrl buildUrl(final ProfileResult profileResult, final PyroscopeReporterConfig config) {
        ProfileRequest request = profileResult.getRequest();
        Set<String> events = request.getEvents();
        HttpUrl.Builder builder = HttpUrl.parse(config.getPyroscopeServerAddress())
                .newBuilder()
                .addPathSegment("ingest")
                .addQueryParameter("name", getName(profileResult, config))
                // when multi events containing ALLOC event, verified that units can be objects or samples
                .addQueryParameter("units", (events.contains(Events.ALLOC) && events.size() == 1) ? "objects" : "samples")
                .addQueryParameter("aggregationType", "sum")
                .addQueryParameter("sampleRate", getIntervalInHz(profileResult.getRequest()))
                .addQueryParameter("from", asEpochSecondsString(profileResult.getStartTime()))
                .addQueryParameter("until", asEpochSecondsString(profileResult.getEndTime()))
                .addQueryParameter("spyName", config.getSpyName());
        if (request.getFormat() == Format.JFR)
            builder.addQueryParameter("format", "jfr");
        return builder.build();
    }

    private RequestBody createRequestBody(MediaType mediaType, InputStream bodyInputStream) {
        return createRequestBody(mediaType,
                bufferedSink -> {
                    try {
                        ioUtils.copy(bodyInputStream, bufferedSink.outputStream());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private RequestBody createRequestBody(MediaType mediaType, Consumer<BufferedSink> consumer) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) {
                consumer.accept(bufferedSink);
            }
        };
    }

    private RequestBody createLabelsBody(ProfileResult profileResult) {
        RequestBody labelsBody = createRequestBody(MultipartBody.FORM,
                (bufferedSink -> {
                    try {
                        profileResult.getLabels().writeTo(bufferedSink.outputStream());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
        if (config.getCompressionLevelJFR() != Deflater.NO_COMPRESSION) {
            labelsBody = GzipSink.gzip(labelsBody, config.getCompressionLevelJFR());
        }
        return labelsBody;
    }

    private MultipartBody createMultipartBodyForJFR(ProfileResult profileResult) {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        RequestBody jfrBody = createRequestBody(MultipartBody.FORM, profileResult.getResultInputStream());
        if (config.getCompressionLevelJFR() != Deflater.NO_COMPRESSION) {
            jfrBody = GzipSink.gzip(jfrBody, config.getCompressionLevelJFR());
        }
        multipartBodyBuilder.addFormDataPart("jfr", "jfr", jfrBody);
        if (profileResult.getLabels() != null) {
            RequestBody labelsBody = createLabelsBody(profileResult);
            multipartBodyBuilder.addFormDataPart("labels", "labels", labelsBody);
        }
        return multipartBodyBuilder.build();
    }

    private Response uploadProfileResult(final ProfileResult profileResult, final PyroscopeReporterConfig config) throws IOException {
        final HttpUrl url = buildUrl(profileResult, config);
            final RequestBody requestBody;
            final ProfileRequest profileRequest = profileResult.getRequest();
            if (profileRequest.getFormat() == Format.JFR) {
                requestBody = createMultipartBodyForJFR(profileResult);
            } else {
                requestBody = createRequestBody(MultipartBody.FORM, profileResult.getResultInputStream());
            }
            Request.Builder request = new Request.Builder()
                    .post(requestBody)
                    .url(url);
            return httpClient.newCall(request.build()).execute();
    }

    private String asEpochSecondsString(LocalDateTime localDateTime) {
        return Long.toString(localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond());
    }

    private String getIntervalInHz(ProfileRequest profileRequest) {
        return String.valueOf(TimeUnit.SECONDS.toNanos(1) / profileRequest.getSamplingIntervalTimeUnit().toNanos(profileRequest.getSamplingInterval()));
    }
}
