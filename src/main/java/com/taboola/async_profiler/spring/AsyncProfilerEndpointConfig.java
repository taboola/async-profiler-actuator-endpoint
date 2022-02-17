package com.taboola.async_profiler.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.taboola.async_profiler.api.facade.AsyncProfilerCommandsFactory;
import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.profiler.AsyncProfilerSupplier;
import com.taboola.async_profiler.utils.IOUtils;
import com.taboola.async_profiler.utils.ThreadUtils;

@Configuration
@Lazy
@ConditionalOnProperty(name = "com.taboola.asyncProfilerEndpoint.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncProfilerEndpointConfig {

    @Bean
    public AsyncProfilerSupplier asyncProfilerSupplier(IOUtils ioUtils, AsyncProfilerEndpointConfigurations asyncProfilerConfig) {
        return new AsyncProfilerSupplier(ioUtils, asyncProfilerConfig.getLibPath());
    }

    @Bean
    @ConfigurationProperties("com.taboola.asyncProfiler")
    public AsyncProfilerEndpointConfigurations asyncProfilerFacadeConfig() {
        return new AsyncProfilerEndpointConfigurations();
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
                                                   AsyncProfilerEndpointConfigurations asyncProfilerConfig,
                                                   ThreadUtils threadUtils,
                                                   IOUtils ioUtils) {
        return new AsyncProfilerFacade(asyncProfilerSupplier.getProfiler(),
                asyncProfilerConfig.getProfileTempFileName(),
                profilerCommandsFactory,
                threadUtils,
                ioUtils);
    }

    @Bean
    public AsyncProfilerEndpoint asyncProfilerEndpoint(AsyncProfilerFacade asyncProfilerFacade,
                                                       AsyncProfilerEndpointConfigurations configurations) {
        return new AsyncProfilerEndpoint(asyncProfilerFacade, configurations.isSensitiveEndpoint());
    }
}
