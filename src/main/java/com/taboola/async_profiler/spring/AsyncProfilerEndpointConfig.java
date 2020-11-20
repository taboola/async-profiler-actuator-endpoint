package com.taboola.async_profiler.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.ProfilerCommandsFactory;
import com.taboola.async_profiler.api.facade.FlameGraphFactory;
import com.taboola.async_profiler.api.facade.profiler.AsyncProfilerProvider;
import com.taboola.async_profiler.utils.ThreadUtils;

@Configuration
@Lazy
public class AsyncProfilerEndpointConfig {

    @Bean
    public AsyncProfilerProvider asyncProfilerProvider(@Value("${com.taboola.asyncprofiler.lib.path:/opt/async-profiler/build/libasyncProfiler.so}") String libPath) {
        return new AsyncProfilerProvider(libPath);
    }

    @Bean
    public ProfilerCommandsFactory profilerCommandsFactory() {
        return new ProfilerCommandsFactory();
    }

    @Bean
    public FlameGraphFactory flameGraphFactory() {
        return new FlameGraphFactory();
    }

    @Bean
    public ThreadUtils threadUtils() {
        return new ThreadUtils();
    }

    @Bean
    public AsyncProfilerFacade asyncProfilerFacade(AsyncProfilerProvider asyncProfilerProvider,
                                                   ProfilerCommandsFactory profilerCommandsFactory,
                                                   FlameGraphFactory flameGraphFactory,
                                                   ThreadUtils threadUtils) {
        return new AsyncProfilerFacade(asyncProfilerProvider.getProfiler(), profilerCommandsFactory, flameGraphFactory, threadUtils);
    }

    @Bean
    @ConditionalOnProperty(name = "com.taboola.asyncprofiler.endpoint.enabled", havingValue = "true", matchIfMissing = true)
    public AsyncProfilerEndPoint asyncProfilerEndPoint(AsyncProfilerFacade asyncProfilerFacade) {
        return new AsyncProfilerEndPoint(asyncProfilerFacade);
    }
}
