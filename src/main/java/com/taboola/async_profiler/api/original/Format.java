package com.taboola.async_profiler.api.original;

public enum Format {
    FLAT(false),
    TRACES(false),
    COLLAPSED(false),
    FLAMEGRAPH(false),
    TREE(false),
    JFR(true);

    private boolean binary;

    Format(boolean binary) {
        this.binary = binary;
    }

    public boolean isBinary() {
        return binary;
    }
}
