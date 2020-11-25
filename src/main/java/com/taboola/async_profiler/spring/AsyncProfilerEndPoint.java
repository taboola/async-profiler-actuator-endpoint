package com.taboola.async_profiler.spring;

import java.io.OutputStream;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.ProfileRequest;

public class AsyncProfilerEndPoint implements MvcEndpoint {

    private final AsyncProfilerFacade asyncProfilerFacade;
    private final boolean isSensitive;

    public AsyncProfilerEndPoint(AsyncProfilerFacade asyncProfilerFacade, boolean isSensitive) {
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

    @GetMapping(value = "/profile", produces = MediaType.TEXT_HTML_VALUE)
    public void profile(ProfileRequest profileRequest, OutputStream responseOutputStream) {
        asyncProfilerFacade.profile(profileRequest, responseOutputStream);
    }

    @GetMapping(value = "/stop", produces = MediaType.TEXT_HTML_VALUE)
    public void stop(OutputStream responseOutputStream) {
        asyncProfilerFacade.stop(responseOutputStream);
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
}
