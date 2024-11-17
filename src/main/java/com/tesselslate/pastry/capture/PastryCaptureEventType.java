package com.tesselslate.pastry.capture;

public enum PastryCaptureEventType {
    FRAME,
    ENTITY,
    BLOCKENTITY,
    WORLD_LOAD,
    BLOCK_OUTLINE,
    OPTIONS,
    DIMENSION,
    SYSINFO,
    PROFILER,
    GAMEMODE;

    private static PastryCaptureEventType[] cachedValues = PastryCaptureEventType.values();

    public static PastryCaptureEventType fromInt(int i) throws IndexOutOfBoundsException {
        return cachedValues[i];
    }
}
