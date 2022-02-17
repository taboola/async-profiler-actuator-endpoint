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
        String envName = detectEnvironment();
        try (final InputStream inputStream = AsyncProfilerSupplier.class.getResourceAsStream("/async-profiler-libs/" + envName + "/libasyncProfiler.so")) {
            String pathToLib = ioUtils.createTempFile("libasyncProfiler", ".so");
            ioUtils.copy(inputStream, pathToLib);

            return pathToLib;
        }
    }

    private static String detectEnvironment() {
        final String osName = System.getProperty("os.name");
        final String osArch = System.getProperty("os.arch");

        if ("Linux".equals(osName)) {
            if ("x86_64".equals(osArch)) {
                return "linux-x64";
            } else if ("arm64".equals(osArch)) {
                return "linux-arm64";
            } else {
                throw new RuntimeException("Unsupported Arch " + osArch);
            }
        } else if ("Mac OS X".equals(osName)) {
            return "macos";
        } else {
            throw new RuntimeException("Unsupported OS " + osName);
        }
    }
}
