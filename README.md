# async-profiler-actuator-endpoint

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.taboola/async-profiler-actuator-endpoint/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.taboola/async-profiler-actuator-endpoint)
[![Build Status](https://travis-ci.org/taboola/async-profiler-actuator-endpoint.svg?branch=master)](https://travis-ci.org/taboola/async-profiler-actuator-endpoint)

This project contains a spring boot actuator endpoint implementation which serves as a wrapper over async-profiler (https://github.com/jvm-profiling-tools/async-profiler).
It allows sending an http profile request directly to the profiled spring boot service, and get the flame graph in the response (or in any other supported format).

It was tested with version 1.7.1 of async-profiler.

#### Preconditions:
* The code looks for the async-profiler binary at /opt/async-profiler/build/libasyncProfiler.so. 
So the async-profiler binaries should be located at: /opt/async-profiler/ (or any other path which needs to be configured via the property: com.taboola.asyncprofiler.lib.path)
* The profiled service should be a spring boot service with actuator included.


#### Usage Example:

Just include the endpoint configuration class in your application context configuration class, e.g:
```
@Configuration
@Import(AsyncProfilerEndpointConfig.class) //async-profiler actuator endpoint beans will get created
public class YourSpringConfigurationClass {
}
```

#### API endpoints:
1. /async-profiler/profile - to profile the service (default duration is 60sec with eventType=cpu) and get the flame graph in the response.
2. /async-profiler/stop - to stop a currently active profiling session, if any, and get its profiling output in the response.
3. /async-profiler/events - to get the supported event types.
4. /async-profiler/version - to get the profiler version.