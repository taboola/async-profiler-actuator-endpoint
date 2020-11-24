package com.taboola.async_profiler.api.facade.profiler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AsyncProfilerSupplierTest {

    @Test
    public void testGetProfiler_whenNotExist_shouldReturnEmptyInstance() {
        AsyncProfilerSupplier asyncProfilerSupplier = new AsyncProfilerSupplier(null);
        AsyncProfiler asyncProfiler = asyncProfilerSupplier.getProfiler();

        assertNotNull(asyncProfiler);
        assertTrue(asyncProfiler instanceof EmptyAsyncProfiler);
    }
}