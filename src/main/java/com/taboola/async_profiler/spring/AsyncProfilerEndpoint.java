package com.taboola.async_profiler.spring;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.ProfileRequest;
import com.taboola.async_profiler.api.facade.ProfileResult;

public class AsyncProfilerEndpoint implements MvcEndpoint {

    public static final String BINARY_PROFILE_ATTACHMENT_HEADER_VALUE = "attachment; filename=profile.";

    private final AsyncProfilerFacade asyncProfilerFacade;
    private final boolean isSensitive;

    public AsyncProfilerEndpoint(AsyncProfilerFacade asyncProfilerFacade, boolean isSensitive) {
        this.asyncProfilerFacade = asyncProfilerFacade;
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
        ProfileResult profileResult = asyncProfilerFacade.profile(profileRequest);

        return toResponseEntity(profileResult);
    }

    @GetMapping(value = "/stop", produces = MediaType.ALL_VALUE)
    public ResponseEntity stop() {
        ProfileResult profileResult = asyncProfilerFacade.stop();

        return toResponseEntity(profileResult);
    }

    @GetMapping(value = "/events")
    @ResponseBody
    public String getSupportedEvents() {
        return asyncProfilerFacade.getSupportedEvents();
    }

    @GetMapping(value = "/version")
    @ResponseBody
    public String getVersion() {
        return asyncProfilerFacade.getProfilerVersion();
    }

    private ResponseEntity toResponseEntity(ProfileResult profileResult) {
        ResponseEntity.BodyBuilder responseEntityBodyBuilder = ResponseEntity.ok();
        if (profileResult.getFormat().isBinary()) {
            responseEntityBodyBuilder.header(HttpHeaders.CONTENT_DISPOSITION, BINARY_PROFILE_ATTACHMENT_HEADER_VALUE + profileResult.getFormat().name().toLowerCase());
        }

        return responseEntityBodyBuilder.body(new InputStreamResource(profileResult.getResultInputStream()));
    }
}
