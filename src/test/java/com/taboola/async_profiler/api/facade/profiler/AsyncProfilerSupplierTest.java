package com.taboola.async_profiler.api.facade.profiler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.taboola.async_profiler.utils.IOUtils;

public class AsyncProfilerSupplierTest {

    @Test
    public void testGetProfiler_whenPathToLibIsWrong_shouldReturnEmptyInstance() {
        AsyncProfilerSupplier asyncProfilerSupplier = new AsyncProfilerSupplier(new IOUtils(),"dummy");
        AsyncProfiler asyncProfiler = asyncProfilerSupplier.getProfiler();

        assertNotNull(asyncProfiler);
        assertTrue(asyncProfiler instanceof EmptyAsyncProfiler);
    }

    @Test
    public void testGetProfiler_whenPathToLibIsMissing_shouldReturnBundledInstanceForCurrentEnv() {
        AsyncProfilerSupplier asyncProfilerSupplier = new AsyncProfilerSupplier(new IOUtils(),null);

        String osEnv = null;
        try {
            osEnv = AsyncProfilerSupplier.detectOSEnvironment();
        } catch (Exception e) {}

        AsyncProfiler asyncProfiler = asyncProfilerSupplier.getProfiler();

        assertNotNull(asyncProfiler);
        if (osEnv != null) {
            assertFalse(asyncProfiler instanceof EmptyAsyncProfiler);
        } else {
            assertTrue(asyncProfiler instanceof EmptyAsyncProfiler);
        }
    }
}