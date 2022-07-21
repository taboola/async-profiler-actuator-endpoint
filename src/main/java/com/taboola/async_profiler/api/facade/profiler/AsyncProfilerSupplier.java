package com.taboola.async_profiler.api.facade.profiler;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taboola.async_profiler.api.original.AsyncProfilerImpl;
import com.taboola.async_profiler.utils.IOUtils;

public class AsyncProfilerSupplier {

	private static Logger logger = LoggerFactory.getLogger(AsyncProfilerSupplier.class);

	private final AsyncProfiler asyncProfiler;

	public AsyncProfilerSupplier(IOUtils ioUtils, String configuredLibPath) {
		asyncProfiler = new LazyLoadedAsyncProfiler(() -> loadAsyncProfilerLib(ioUtils, configuredLibPath));
	}

	public AsyncProfiler getProfiler() {
		return asyncProfiler;
	}

    private AsyncProfiler loadAsyncProfilerLib(IOUtils ioUtils, String configuredLibPath) {
        try {
            String libPath = (configuredLibPath != null) ? configuredLibPath : unpackBundledLib(ioUtils);
            logger.info("Loading AsyncProfiler from {}", libPath);
            return AsyncProfilerImpl.getInstance(libPath);

        } catch (Throwable t) {
            logger.error("Failed loading async profiler lib", t);
            //Couldn't load the profiler library, use the empty implementation
            return new EmptyAsyncProfiler(t);
        }
    }

	/**
	 * Detect the relevant lib out of the bundled profiler libs resources and extract it to a temp file
	 *
	 * @return the path to extracted file, if any
	 */
	private String unpackBundledLib(IOUtils ioUtils) throws IOException {
		String osEnvName = detectOSEnvironment();
		try (final InputStream inputStream = AsyncProfilerSupplier.class.getResourceAsStream("/async-profiler-libs/" + osEnvName + "/libasyncProfiler.so")) {
			String pathToLib = ioUtils.createTempFile("libasyncProfiler", ".so");
			ioUtils.copy(inputStream, pathToLib);
			logger.info("Extracted bundled AsyncProfiler for {} to {}", osEnvName, pathToLib);

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
