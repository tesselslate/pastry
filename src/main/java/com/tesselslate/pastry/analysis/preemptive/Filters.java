package com.tesselslate.pastry.analysis.preemptive;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureDimensionEvent;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Provides implementations of various filters for reading captures and
 * analyzing preemptive data.
 */
public final class Filters {
    /**
     * Filters a stream of {@linkplain PastryCaptureEvent capture events}, returning
     * false for all events that do not take place in the dimension specified by
     * {@code name}.
     *
     * @param name The dimension to filter for
     * @return A predicate which filters out events not in that dimension
     */
    public static Predicate<PastryCaptureEvent> dimension(String name) {
        return new Predicate<>() {
            private boolean inDimension = true;

            @Override
            public boolean test(PastryCaptureEvent event) {
                if (event instanceof PastryCaptureDimensionEvent dimensionEvent) {
                    this.inDimension = dimensionEvent.name.equals(name);
                }

                return this.inDimension && !(event instanceof PastryCaptureDimensionEvent);
            }
        };
    }

    /**
     * Filters a stream of {@linkplain PreemptiveReading preemptive readings} for
     * "strict consistency", where the following properties of all frames in the
     * reading are equal:
     *
     * <ul>
     * <li>Whether a targeted block is present</li>
     * <li>The active options (window size, render distance, etc)</li>
     * <li>The number of visible entities</li>
     * <li>The set of visible block entities</li>
     * </ul>
     *
     * @return A predicate which filters out not-strictly-consistent readings
     */
    public static Predicate<PreemptiveReading> strictConsistency() {
        return reading -> {
            PreemptiveFrame[] frames = reading.frames();
            PreemptiveFrame a = frames[0];

            for (int i = 1; i < frames.length; i++) {
                PreemptiveFrame b = frames[i];

                if ((a.blockOutline() == null) != (b.blockOutline() == null)) {
                    return false;
                }
                if (!a.options().equals(b.options())) {
                    return false;
                }
                if (a.entities().length != b.entities().length) {
                    return false;
                }
                if (!Arrays.equals(a.blockEntities(), b.blockEntities())) {
                    return false;
                }
            }

            return true;
        };
    }
}
