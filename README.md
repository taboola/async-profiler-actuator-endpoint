# async-profiler-actuator-endpoint

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.taboola/async-profiler-actuator-endpoint/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.taboola/async-profiler-actuator-endpoint)
[![Build Status](https://travis-ci.org/taboola/async-profiler-actuator-endpoint.svg?branch=master)](https://travis-ci.org/taboola/async-profiler-actuator-endpoint)

This project contains a spring boot actuator endpoint implementation which serves as a wrapper for [Async Profiler](https://github.com/jvm-profiling-tools/async-profiler).

It allows controlling the profiler via simple HTTP GET requests to the profiled service itself. 

You can either send a blocking profile request and get the flame graph in the response, or send a non-blocking request to start a continuous-profiling session which will periodically report profile results to a dedicated reporter. 

#### Async Profiler Library
The profiler library is already bundled in this project. If you want to use your own custom version, you can just override it by configuring the path to yours, e.g: 
```com.taboola.asyncProfiler.libPath=/opt/async-profiler/build/libasyncProfiler.so```

#### Continuous Profiling
As mentioned above, we also support continuous profiling to periodically run the profiler and report the results to a dedicated reporter.
We provide a default PyroscopeReporter, to report the snapshots to a configurable [Pyroscope](https://github.com/pyroscope-io/pyroscope) server, but you can also create a different ProfileResultsReporter bean to override the default one with your own custom implementation.

You can start/stop the session from the endpoint, but you can also configure it to start automatically when the service starts by configuring:
```com.taboola.asyncProfiler.continuousProfiling.startOnInit=true```


#### Getting Started
Add the dependency:
```
<dependency>
  <groupId>com.taboola</groupId>
  <artifactId>async-profiler-actuator-endpoint</artifactId>
</dependency>
```

Wire it into your app by including the endpoint configuration class in your application context configuration class, e.g:
```
@Configuration
@Import(AsyncProfilerEndpointConfig.class) 
public class YourSpringConfigurationClass {
}
```

#### Endpoints
1. `/async-profiler/profile` - to profile the service and get the flame graph in the response (blocking call, default is cpu profiling for 60sec).
    
    Or, for example, profile multiple events together for 30 seconds and get it as a .jfr file attachment in the response:

   `/async-profiler/profile?events=alloc,cpu,lock&durationSeconds=30`

    Check [ProfileRequest](https://github.com/taboola/async-profiler-actuator-endpoint/blob/main/src/main/java/com/taboola/async_profiler/api/facade/ProfileRequest.java) to see the possible request parameters.


2. `/async-profiler/stop` - to stop a currently active profile request, if any, and get its profiling output in the response.


3. `/async-profiler/start-continuous` - to start a continuous profiling session. 

    This will use the default parameters (cpu profiling in 10-seconds snapshots), you can override them with the same parameters that are available for profile requests.


4. `/async-profiler/stop-continuous` - to stop a continuous profiling session.


5. `/async-profiler/events` - to get the supported event types.


6. `/async-profiler/version` - to get the loaded profiler version.