package com.taboola.async_profiler.spring;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
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

@Endpoint(id = "async-profiler")
public class AsyncProfilerEndpoint {

    public static final String BINARY_PROFILE_ATTACHMENT_HEADER_VALUE = "attachment; filename=profile.";

    private final AsyncProfilerService asyncProfilerService;

    public AsyncProfilerEndpoint(AsyncProfilerService asyncProfilerService) {
        this.asyncProfilerService = asyncProfilerService;
    }

    @WriteOperation
    @GetMapping(value = "/profile", produces = MediaType.ALL_VALUE)
    public ResponseEntity profile(ProfileRequest profileRequest) {
        ProfileResult profileResult = asyncProfilerService.profile(profileRequest);

        return toResponseEntity(profileResult);
    }

    @WriteOperation
    @GetMapping(value = "/stop", produces = MediaType.ALL_VALUE)
    public ResponseEntity stop() {
        ProfileResult profileResult = asyncProfilerService.stop();

        return toResponseEntity(profileResult);
    }

    @WriteOperation
    @GetMapping(value = "/start-continuous", produces = MediaType.ALL_VALUE)
    @ResponseBody
    public String startContinuous(ContinuousProfilingSnapshotRequest snapshotRequest) {
        asyncProfilerService.startContinuousProfiling(snapshotRequest);

        return "Started continuous profiling with: " + snapshotRequest;
    }

    @WriteOperation
    @GetMapping(value = "/stop-continuous", produces = MediaType.ALL_VALUE)
    @ResponseBody
    public String stopContinuous() {
        asyncProfilerService.stopContinuousProfiling();

        return "Stopped continuous profiling";
    }

    @ReadOperation
    @GetMapping(value = "/events")
    @ResponseBody
    public String getSupportedEvents() {
        return asyncProfilerService.getSupportedEvents();
    }

    @ReadOperation
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
