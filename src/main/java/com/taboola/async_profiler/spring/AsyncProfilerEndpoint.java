package com.taboola.async_profiler.spring;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taboola.async_profiler.api.AsyncProfilerService;
import com.taboola.async_profiler.api.continuous.ContinuousProfilingSnapshotRequest;
import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;

public class AsyncProfilerEndpoint implements MvcEndpoint {

    public static final String BINARY_PROFILE_ATTACHMENT_HEADER_VALUE = "attachment; filename=profile.";

    private final AsyncProfilerService asyncProfilerService;
    private final boolean isSensitive;

    public AsyncProfilerEndpoint(AsyncProfilerService asyncProfilerService, boolean isSensitive) {
        this.asyncProfilerService = asyncProfilerService;
        this.isSensitive = isSensitive;
    }

    @Override
    public String getPath() {
        return "/async-profiler";
    }

    @Override
    public boolean isSensitive() {
        return isSensitive;
    }

    @Override
    public Class<? extends Endpoint> getEndpointType() {
        return null;
    }

    @GetMapping(value = "/profile", produces = MediaType.ALL_VALUE)
    public ResponseEntity profile(ProfileRequest profileRequest) {
        ProfileResult profileResult = asyncProfilerService.profile(profileRequest);

        return toResponseEntity(profileResult);
    }

    @GetMapping(value = "/stop", produces = MediaType.ALL_VALUE)
    public ResponseEntity stop() {
        ProfileResult profileResult = asyncProfilerService.stop();

        return toResponseEntity(profileResult);
    }

    @GetMapping(value = "/start-continuous", produces = MediaType.ALL_VALUE)
    @ResponseBody
    public String startContinuous(ContinuousProfilingSnapshotRequest snapshotRequest) {
        asyncProfilerService.startContinuousProfiling(snapshotRequest);

        return "Started continuous profiling with: " + snapshotRequest;
    }

    @GetMapping(value = "/stop-continuous", produces = MediaType.ALL_VALUE)
    @ResponseBody
    public String stopContinuous() {
        asyncProfilerService.stopContinuousProfiling();

        return "Stopped continuous profiling";
    }

    @GetMapping(value = "/events")
    @ResponseBody
    public String getSupportedEvents() {
        return asyncProfilerService.getSupportedEvents();
    }

    @GetMapping(value = "/version")
    @ResponseBody
    public String getVersion() {
        return asyncProfilerService.getProfilerVersion();
    }

    private ResponseEntity toResponseEntity(ProfileResult profileResult) {
        ResponseEntity.BodyBuilder responseEntityBodyBuilder = ResponseEntity.ok();
        if (profileResult.getRequest().getFormat().isBinary()) {
            responseEntityBodyBuilder.header(HttpHeaders.CONTENT_DISPOSITION, BINARY_PROFILE_ATTACHMENT_HEADER_VALUE + profileResult.getRequest().getFormat().name().toLowerCase());
        }

        return responseEntityBodyBuilder.body(new InputStreamResource(profileResult.getResultInputStream()));
    }
}
