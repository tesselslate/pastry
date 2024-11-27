package com.tesselslate.pastry.analysis.preemptive;

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
}
