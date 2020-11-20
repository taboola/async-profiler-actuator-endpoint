package com.taboola.async_profiler.api.facade;

import com.taboola.async_profiler.api.original.FlameGraph;

public class FlamGraphFactory {

    public FlameGraph get() {
        return new FlameGraph();
    }
}
