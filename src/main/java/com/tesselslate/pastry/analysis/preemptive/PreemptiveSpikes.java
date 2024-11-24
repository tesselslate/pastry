package com.tesselslate.pastry.analysis.preemptive;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes one or more {@linkplain PreemptiveReading preemptive readings} to
 * provide statistics on their profiler values.
 */
public class PreemptiveSpikes {
    private final ArrayList<PreemptiveReading> readings;

    public PreemptiveSpikes() {
        this.readings = new ArrayList<>();
    }

    public void add(PreemptiveReading reading) {
        this.readings.add(reading);
    }

    public void add(PreemptiveSpikes spikes) {
        this.readings.addAll(spikes.readings);
    }

    public List<PreemptiveReading> getReadings() {
        return this.readings;
    }
}
