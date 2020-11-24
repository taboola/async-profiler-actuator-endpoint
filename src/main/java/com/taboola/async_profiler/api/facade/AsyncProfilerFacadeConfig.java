package com.taboola.async_profiler.api.facade;

public class AsyncProfilerFacadeConfig {

    private String profileTempFileName = "async_profiler_endpoint_dump";
    private String successfulStartCommandResponse = "OK";
    private String successfulStopCommandResponse = "OK";

    public String getProfileTempFileName() {
        return profileTempFileName;
    }

    public void setProfileTempFileName(String profileTempFileName) {
        this.profileTempFileName = profileTempFileName;
    }

    public String getSuccessfulStartCommandResponse() {
        return successfulStartCommandResponse;
    }

    public void setSuccessfulStartCommandResponse(String successfulStartCommandResponse) {
        this.successfulStartCommandResponse = successfulStartCommandResponse;
    }

    public String getSuccessfulStopCommandResponse() {
        return successfulStopCommandResponse;
    }

    public void setSuccessfulStopCommandResponse(String successfulStopCommandResponse) {
        this.successfulStopCommandResponse = successfulStopCommandResponse;
    }
}
