package com.taboola.async_profiler.api.serviceconfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorServiceConfig {
    int corePoolSize;
    int maxPoolSize;
    int queueCapacity;
}
