package com.tesselslate.pastry.analysis.preemptive;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureDimensionEvent;

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
}
