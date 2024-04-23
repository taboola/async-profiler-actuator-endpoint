package com.taboola.async_profiler.api.original;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CStack {
    FP("fp"),
    DWARF("dwarf"),
    LBR("lbr"),
    VM("vm"),
    NO("no");

    private final String mode;
}
