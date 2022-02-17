package com.taboola.async_profiler.api.facade.profiler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.taboola.async_profiler.utils.IOUtils;

public class AsyncProfilerSupplierTest {

    @Test
    public void testGetProfiler_whenNotExist_shouldReturnEmptyInstance() {
        AsyncProfilerSupplier asyncProfilerSupplier = new AsyncProfilerSupplier(mock(IOUtils.class),null);
        AsyncProfiler asyncProfiler = asyncProfilerSupplier.getProfiler();

        assertNotNull(asyncProfiler);
        assertTrue(asyncProfiler instanceof EmptyAsyncProfiler);
    }
}