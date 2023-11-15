package com.taboola.async_profiler.api.facade.profiler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.taboola.async_profiler.utils.IOUtils;

public class AsyncProfilerSupplierTest {

    @Test
    public void testGetProfiler() {
        AsyncProfilerSupplier asyncProfilerSupplier = new AsyncProfilerSupplier();
        AsyncProfiler asyncProfiler = asyncProfilerSupplier.getProfiler();

        assertNotNull(asyncProfiler);
        assertTrue(asyncProfiler instanceof LazyLoadedAsyncProfiler);

        LazyLoadedAsyncProfiler lazyLoadedAsyncProfiler = (LazyLoadedAsyncProfiler) asyncProfiler;
        assertFalse(lazyLoadedAsyncProfiler.getOrLoadAsyncProfiler() instanceof EmptyAsyncProfiler);
    }

}