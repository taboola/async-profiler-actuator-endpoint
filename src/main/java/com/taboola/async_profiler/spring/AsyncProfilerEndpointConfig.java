package com.taboola.async_profiler.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.taboola.async_profiler.api.serviceconfig.AsyncProfilerServiceConfigurations;
import com.taboola.async_profiler.api.AsyncProfilerService;
import com.taboola.async_profiler.api.continuous.ProfileSnapshotsReporter;
import com.taboola.async_profiler.api.continuous.pyroscope.PyroscopeReporter;
import com.taboola.async_profiler.api.facade.AsyncProfilerCommandsFactory;
import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.profiler.AsyncProfilerSupplier;
import com.taboola.async_profiler.api.serviceconfig.ExecutorServiceConfig;
import com.taboola.async_profiler.utils.IOUtils;
import com.taboola.async_profiler.utils.NetUtils;
import com.taboola.async_profiler.utils.ThreadUtils;

@Configuration
@Lazy
@ConditionalOnProperty(name = "com.taboola.asyncProfilerEndpoint.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncProfilerEndpointConfig {

    @Bean
    @ConfigurationProperties("com.taboola.asyncProfiler")
    public AsyncProfilerServiceConfigurations asyncProfilerServiceConfigurations() {
        return new AsyncProfilerServiceConfigurations();
    }

    @Bean
    public ThreadUtils threadUtils() {
        return new ThreadUtils();
    }

    @Bean
    public NetUtils netUtils() {
        return new NetUtils();
    }

    @Bean
    public IOUtils ioUtils() {
        return new IOUtils();
    }

    @Bean
    public AsyncProfilerCommandsFactory profilerCommandsFactory() {
        return new AsyncProfilerCommandsFactory();
    }

    @Bean
    public AsyncProfilerSupplier asyncProfilerSupplier(IOUtils ioUtils, AsyncProfilerServiceConfigurations asyncProfilerConfig) {
        return new AsyncProfilerSupplier(ioUtils, asyncProfilerConfig.getLibPath());
    }

    @Bean
    public AsyncProfilerFacade asyncProfilerFacade(AsyncProfilerSupplier asyncProfilerSupplier,
                                                   AsyncProfilerCommandsFactory profilerCommandsFactory,
                                                   AsyncProfilerServiceConfigurations asyncProfilerConfig,
                                                   ThreadUtils threadUtils,
                                                   IOUtils ioUtils) {
        return new AsyncProfilerFacade(asyncProfilerSupplier.getProfiler(),
                asyncProfilerConfig.getProfileTempFileName(),
                profilerCommandsFactory,
                threadUtils,
                ioUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProfileSnapshotsReporter profileSnapshotsReporter(AsyncProfilerServiceConfigurations asyncProfilerConfigurations, IOUtils ioUtils, NetUtils netUtils) {
        //using pyroscope reporter as the default reporter implementation
        //will get called only if no other ProfileSnapshotsReporter bean was provided
        return new PyroscopeReporter(asyncProfilerConfigurations.getContinuousProfiling().getPyroscopeReporter(), ioUtils, netUtils);
    }

    @Bean
    public AsyncProfilerService asyncProfilerService(AsyncProfilerFacade asyncProfilerFacade,
                                                     AsyncProfilerServiceConfigurations asyncProfilerConfig,
                                                     ProfileSnapshotsReporter profileSnapshotsReporter,
                                                     ThreadUtils threadUtils) {
        ExecutorServiceConfig snapshotsReporterExecutorServiceConf = asyncProfilerConfig.getContinuousProfiling().getSnapshotsReporterExecutorService();
        AsyncProfilerService asyncProfilerService = new AsyncProfilerService(asyncProfilerFacade,
                profileSnapshotsReporter,
                threadUtils.newDaemonsExecutorService(1, 1, 1),
                threadUtils.newDaemonsExecutorService(snapshotsReporterExecutorServiceConf.getCorePoolSize(),
                        snapshotsReporterExecutorServiceConf.getMaxPoolSize(),
                        snapshotsReporterExecutorServiceConf.getQueueCapacity()),
                asyncProfilerConfig,
                threadUtils);

        return asyncProfilerService;
    }

    @Bean
    public AsyncProfilerEndpoint asyncProfilerEndpoint(AsyncProfilerService asyncProfilerService,
                                                       @Value("${com.taboola.asyncProfilerEndpoint.sensitive:false}") boolean isSensitive) {
        return new AsyncProfilerEndpoint(asyncProfilerService, isSensitive);
    }
}
