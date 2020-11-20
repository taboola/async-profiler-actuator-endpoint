package com.taboola.async_profiler.spring;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.ProfileRequest;

public class AsyncProfilerEndPoint implements MvcEndpoint {

    private final AsyncProfilerFacade asyncProfilerFacade;

    public AsyncProfilerEndPoint(AsyncProfilerFacade asyncProfilerFacade) {
        this.asyncProfilerFacade = asyncProfilerFacade;
    }

    @Override
    public String getPath() {
        return "/async-profiler";
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public Class<? extends Endpoint> getEndpointType() {
        return null;
    }

    @GetMapping(value = "/profile", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String profile(ProfileRequest profilingRequest) {
        return asyncProfilerFacade.profile(profilingRequest);
    }

    @GetMapping(value = "/events")
    @ResponseBody
    public String getSupportedEvents() {
        return asyncProfilerFacade.getSupportedEvents();
    }
}
