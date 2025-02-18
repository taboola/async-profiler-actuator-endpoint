package com.taboola.async_profiler.spring;

import com.taboola.async_profiler.api.LabelsWrapper;
import com.taboola.async_profiler.api.continuous.pyroscope.PyroscopeReporterConfig;
import io.pyroscope.okhttp3.OkHttpClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.taboola.async_profiler.api.serviceconfig.AsyncProfilerServiceConfigurations;
import com.taboola.async_profiler.api.AsyncProfilerService;
import com.taboola.async_profiler.api.continuous.ProfileResultsReporter;
import com.taboola.async_profiler.api.continuous.pyroscope.PyroscopeReporter;
import com.taboola.async_profiler.api.facade.AsyncProfilerCommandsFactory;
import com.taboola.async_profiler.api.facade.AsyncProfilerFacade;
import com.taboola.async_profiler.api.facade.profiler.AsyncProfilerSupplier;
import com.taboola.async_profiler.api.serviceconfig.ExecutorServiceConfig;
import com.taboola.async_profiler.utils.IOUtils;
import com.taboola.async_profiler.utils.ThreadUtils;

import java.time.Duration;

@Configuration
@Lazy
@ConditionalOnProperty(name = "com.taboola.asyncProfilerEndpoint.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncProfilerEndpointConfig {

    @Bean
    @ConfigurationProperties("com.taboola.async-profiler")
    public AsyncProfilerServiceConfigurations asyncProfilerServiceConfigurations() {
        return new AsyncProfilerServiceConfigurations();
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
    public OkHttpClient okHttpClient(AsyncProfilerServiceConfigurations config) {
        PyroscopeReporterConfig reporterConfig = config.getContinuousProfiling().getPyroscopeReporter();
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(reporterConfig.getConnectTimeoutMillis()))
                .readTimeout(Duration.ofMillis(reporterConfig.getReadTimeoutMillis()))
                .callTimeout(Duration.ofMillis(reporterConfig.getCallTimeoutMillis()))
                .build();
    }

    @Bean
    public AsyncProfilerCommandsFactory profilerCommandsFactory() {
        return new AsyncProfilerCommandsFactory();
    }

    @Bean
    public AsyncProfilerSupplier asyncProfilerSupplier(IOUtils ioUtils, AsyncProfilerServiceConfigurations asyncProfilerConfig) {
        return new AsyncProfilerSupplier();
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
    public ProfileResultsReporter profileResultsReporter(AsyncProfilerServiceConfigurations asyncProfilerConfigurations, IOUtils ioUtils, OkHttpClient httpClient) {
        //using pyroscope reporter as the default reporter implementation
        //will get called only if no other ProfileResultsReporter bean was provided
        return new PyroscopeReporter(asyncProfilerConfigurations.getContinuousProfiling().getPyroscopeReporter(), ioUtils, httpClient);
    }

    @Bean
    public AsyncProfilerService asyncProfilerService(AsyncProfilerFacade asyncProfilerFacade,
                                                     AsyncProfilerServiceConfigurations asyncProfilerConfig,
                                                     ProfileResultsReporter profileResultsReporter,
                                                     ThreadUtils threadUtils) {
        ExecutorServiceConfig snapshotsReporterExecutorServiceConf = asyncProfilerConfig.getContinuousProfiling().getSnapshotsReporterExecutorService();
        AsyncProfilerService asyncProfilerService = new AsyncProfilerService(asyncProfilerFacade,
                profileResultsReporter,
                threadUtils.newDaemonsExecutorService(1, 1, 1),
                threadUtils.newDaemonsExecutorService(snapshotsReporterExecutorServiceConf.getCorePoolSize(),
                        snapshotsReporterExecutorServiceConf.getMaxPoolSize(),
                        snapshotsReporterExecutorServiceConf.getQueueCapacity()),
                asyncProfilerConfig,
                threadUtils);

        LabelsWrapper.setAsyncProfilerService(asyncProfilerService);
        return asyncProfilerService;
    }

    @Bean
    public AsyncProfilerEndpoint asyncProfilerEndpoint(AsyncProfilerService asyncProfilerService) {
        return new AsyncProfilerEndpoint(asyncProfilerService);
    }
}
