package com.tesselslate.pastry.analysis.preemptive;

import java.util.Arrays;
import java.util.List;

/**
 * Processes one or more {@linkplain PreemptiveStronghold strongholds} to
 * provide statistics on their preemptive spikes.
 *
 * @see PreemptiveReading
 */
public class PreemptiveAnalysis {
    public final PreemptiveSpikes valid;
    public final PreemptiveSpikes invalid;

    public PreemptiveAnalysis() {
        this.valid = new PreemptiveSpikes();
        this.invalid = new PreemptiveSpikes();
    }

    public void process(PreemptiveStronghold stronghold) {
        stronghold.readings.stream().filter(PreemptiveReading::isConsistent).forEach(reading -> {
            boolean isSpike = reading.all(frame -> Arrays.stream(frame.blockEntities())
                    .filter(event -> event.name.equals("mob_spawner") && event.data.equals("silverfish")).findAny()
                    .isPresent());

            if (isSpike) {
                this.valid.add(reading);
            } else {
                this.invalid.add(reading);
            }
        });
    }

    public void process(List<PreemptiveStronghold> strongholds) {
        strongholds.forEach(this::process);
    }
}
