package com.tesselslate.pastry.interfaces;

import com.tesselslate.pastry.capture.structure.PastryStructureCache;

public interface PastryServerWorld {
    /**
     * Returns a {@link PastryStructureCache} for this world or creates a new
     * structure cache if one does not yet exist.
     *
     * @return The {@link PastryStructureCache} for this world
     */
    public PastryStructureCache pastry$getStructureCache();
}
