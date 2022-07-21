package com.taboola.async_profiler.api.facade.profiler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.taboola.async_profiler.utils.IOUtils;

public class AsyncProfilerSupplierTest {

    @Test
    public void testGetProfiler_whenPathToLibIsWrong_shouldReturnEmptyInstance() {
        AsyncProfilerSupplier asyncProfilerSupplier = new AsyncProfilerSupplier(new IOUtils(),"dummy");
        AsyncProfiler asyncProfiler = asyncProfilerSupplier.getProfiler();

        assertNotNull(asyncProfiler);
        assertTrue(asyncProfiler instanceof LazyLoadedAsyncProfiler);

        LazyLoadedAsyncProfiler lazyLoadedAsyncProfiler = (LazyLoadedAsyncProfiler) asyncProfiler;
        assertTrue(lazyLoadedAsyncProfiler.getOrLoadAsyncProfiler() instanceof EmptyAsyncProfiler);
    }

    @Test
    @Ignore("Use locally for manual tests")
    public void testGetProfiler_whenPathToLibIsMissing_shouldReturnBundledInstanceForCurrentEnv() {
        AsyncProfilerSupplier asyncProfilerSupplier = new AsyncProfilerSupplier(new IOUtils(),null);

        String osEnv = null;
        try {
            osEnv = AsyncProfilerSupplier.detectOSEnvironment();
        } catch (Exception e) {}

        AsyncProfiler asyncProfiler = asyncProfilerSupplier.getProfiler();

        assertNotNull(asyncProfiler);
        assertTrue(asyncProfiler instanceof LazyLoadedAsyncProfiler);
        LazyLoadedAsyncProfiler lazyLoadedAsyncProfiler = (LazyLoadedAsyncProfiler) asyncProfiler;
        if (osEnv != null) {
            assertFalse(lazyLoadedAsyncProfiler.getOrLoadAsyncProfiler() instanceof EmptyAsyncProfiler);
        } else {
            assertTrue(lazyLoadedAsyncProfiler.getOrLoadAsyncProfiler() instanceof EmptyAsyncProfiler);
        }
    }
}