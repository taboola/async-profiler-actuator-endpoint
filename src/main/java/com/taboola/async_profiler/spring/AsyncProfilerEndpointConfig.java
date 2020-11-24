package com.taboola.async_profiler.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.taboola.async_profiler.api.facade.AsyncProfilerCommandsFactory;
import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.AsyncProfilerFacadeConfig;
import com.taboola.async_profiler.api.facade.profiler.AsyncProfilerSupplier;
import com.taboola.async_profiler.utils.IOUtils;
import com.taboola.async_profiler.utils.ThreadUtils;

@Configuration
@Lazy
@ConditionalOnProperty(name = "com.taboola.asyncprofiler.endpoint.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncProfilerEndpointConfig {

    @Bean
    public AsyncProfilerSupplier asyncProfilerSupplier(@Value("${com.taboola.asyncprofiler.lib.path:/opt/async-profiler/build/libasyncProfiler.so}") String libPath) {
        return new AsyncProfilerSupplier(libPath);
    }

    @Bean
    @ConfigurationProperties("com.taboola.asyncprofiler.facade")
    public AsyncProfilerFacadeConfig asyncProfilerFacadeConfig() {
        return new AsyncProfilerFacadeConfig();
    }

    @Bean
    public AsyncProfilerCommandsFactory profilerCommandsFactory() {
        return new AsyncProfilerCommandsFactory();
    }

    @Bean
    public ThreadUtils threadUtils() {
        return new ThreadUtils();
    }

    @Bean
    public IOUtils ioUtils() {
        return new IOUtils();
    }

    @Bean
    public AsyncProfilerFacade asyncProfilerFacade(AsyncProfilerSupplier asyncProfilerSupplier,
                                                   AsyncProfilerCommandsFactory profilerCommandsFactory,
                                                   AsyncProfilerFacadeConfig asyncProfilerFacadeConfig,
                                                   ThreadUtils threadUtils,
                                                   IOUtils ioUtils) {
        return new AsyncProfilerFacade(asyncProfilerSupplier.getProfiler(), asyncProfilerFacadeConfig, profilerCommandsFactory, threadUtils, ioUtils);
    }

    @Bean
    public AsyncProfilerEndPoint asyncProfilerEndPoint(AsyncProfilerFacade asyncProfilerFacade) {
        return new AsyncProfilerEndPoint(asyncProfilerFacade);
    }
}
