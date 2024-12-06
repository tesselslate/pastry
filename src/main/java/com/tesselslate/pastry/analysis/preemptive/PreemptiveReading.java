package com.tesselslate.pastry.analysis.preemptive;

import com.tesselslate.pastry.capture.events.PastryCaptureBlockEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureEntityEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

/**
 * Contains information about a single "reading" obtained by the user during
 * preemptive navigation.
 *
 * A preemptive reading consists of a continuous run of frames where the user
 * was located within the stronghold and the F3 profiler menu was open.
 *
 * @see PreemptiveFrame
 */
public record PreemptiveReading(@NotNull PreemptiveFrame[] frames) {
    /**
     * Tests whether all frames of this reading are accepted by {@code predicate}.
     *
     * @return Whether all frames of this reading are accepted by {@code predicate}
     */
    public boolean all(Predicate<PreemptiveFrame> predicate) {
        for (PreemptiveFrame frame : this.frames) {
            if (!predicate.test(frame)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the list of all {@linkplain PastryCaptureBlockEntityEvent block
     * entities} in this preemptive reading. Assumes that the reading is strictly
     * consistent.
     *
     * @return The list of all block entities in this reading
     */
    public List<PastryCaptureBlockEntityEvent> blockEntities() {
        return Arrays.asList(this.frames[0].blockEntities());
    }

    /**
     * Returns the list of all {@linkplain PastryCaptureEntityEvent entities} in
     * this preemptive reading. Assumes that the reading is strictly consistent.
     *
     * @return The list of all entities in this reading
     */
    public List<PastryCaptureEntityEvent> entities() {
        return Arrays.asList(this.frames[0].entities());
    }
}
