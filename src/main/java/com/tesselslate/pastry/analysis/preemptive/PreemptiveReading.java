package com.tesselslate.pastry.analysis.preemptive;

import java.util.Arrays;
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
     * Returns whether or not the given preemptive reading is consistent. A
     * preemptive reading is considered consistent if all of its frames either
     * target or do not target a block, have the same game options, and have the
     * same set of visible entities and block entities.
     *
     * @return Whether the given preemptive reading is consistent
     */
    public boolean isConsistent() {
        PreemptiveFrame a = this.frames[0];

        for (int i = 1; i < frames.length; i++) {
            PreemptiveFrame b = this.frames[i];

            if ((a.blockOutline() == null) != (b.blockOutline() == null)) {
                return false;
            }
            if (!a.options().equals(b.options())) {
                return false;
            }
            if (!Arrays.equals(a.entities(), b.entities())) {
                return false;
            }
            if (!Arrays.equals(a.blockEntities(), b.blockEntities())) {
                return false;
            }
        }

        return true;
    }
}
