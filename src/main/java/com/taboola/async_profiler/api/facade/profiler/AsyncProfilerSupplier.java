package com.taboola.async_profiler.api.facade.profiler;

import java.io.IOException;
import java.io.InputStream;

import com.taboola.async_profiler.api.original.AsyncProfilerImpl;
import com.taboola.async_profiler.utils.IOUtils;

public class AsyncProfilerSupplier {

    private AsyncProfiler asyncProfiler;

    public AsyncProfilerSupplier(IOUtils ioUtils, String configuredLibPath) {
        try {
            String libPath = (configuredLibPath != null) ? configuredLibPath : unpackBundledLib(ioUtils);
            asyncProfiler = AsyncProfilerImpl.getInstance(libPath);

        } catch (Throwable t) {
            //Couldn't load the profiler library, use the empty implementation
            asyncProfiler = new EmptyAsyncProfiler(t);
        }
    }

    /**
     * @return the actual {@link AsyncProfilerImpl} when the native lib was loaded successfully (it is present and valid),
     * otherwise return an {@link EmptyAsyncProfiler}.
     */
    public AsyncProfiler getProfiler() {
        return asyncProfiler;
    }

    /**
     * Detect the relevant lib out of the bundled profiler libs resources and extract it to a temp file
     * @return the path to extracted file, if any
     * */
    private String unpackBundledLib(IOUtils ioUtils) throws IOException {
        String osEnvName = detectOSEnvironment();
        try (final InputStream inputStream = AsyncProfilerSupplier.class.getResourceAsStream("/async-profiler-libs/" + osEnvName + "/libasyncProfiler.so")) {
            String pathToLib = ioUtils.createTempFile("libasyncProfiler", ".so");
            ioUtils.copy(inputStream, pathToLib);

            return pathToLib;
        }
    }

    static String detectOSEnvironment() {
        final String osName = System.getProperty("os.name");
        final String osArch = System.getProperty("os.arch");

        if (osName != null && osName.startsWith("Linux")) {
            if (osArch != null && osArch.contains("arm")) {
                return "linux-arm64";
            } else if (osArch != null && osArch.contains("64")) {
                return "linux-x64";
            } else {
                throw new RuntimeException("Unsupported Arch: " + osArch);
            }
        } else if (osName != null && osName.startsWith("Mac")) {
            return "macos";
        } else {
            throw new RuntimeException("Unsupported OS: " + osName);
        }
    }
}
