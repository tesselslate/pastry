package com.tesselslate.pastry.analysis.preemptive;

import com.tesselslate.pastry.capture.events.PastryCaptureBlockEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureBlockOutlineEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureFrameEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureOptionsEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureProfilerEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains information about the state of the game renderer (settings, visible
 * entities and block entities) and the profiler results of the game renderer
 * for a given frame.
 *
 * @see PastryCaptureFrameEvent
 * @see PastryCaptureEntityEvent
 * @see PastryCaptureBlockEntityEvent
 * @see PastryCaptureBlockOutlineEvent
 * @see PastryCaptureOptionsEvent
 * @see PastryCaptureProfilerEvent
 */
public final record PreemptiveFrame(
        @NotNull PastryCaptureFrameEvent frame,
        @NotNull PastryCaptureOptionsEvent options,
        @NotNull PastryCaptureProfilerEvent profiler,
        @Nullable PastryCaptureBlockOutlineEvent blockOutline,
        @NotNull PastryCaptureEntityEvent[] entities,
        @NotNull PastryCaptureBlockEntityEvent[] blockEntities) {
    /**
     * Returns whether or not the given frame is "pure." A frame is considered pure
     * if the only visible block entity is a single silverfish spawner and there are
     * no other visible entities.
     *
     * @return Whether or not the frame is pure
     */
    public boolean isPure() {
        return this.entities == null
                && this.blockEntities.length == 1
                && this.blockEntities[0].name.equals("mob_spawner")
                && this.blockEntities[0].data.equals("silverfish");
    }
}
