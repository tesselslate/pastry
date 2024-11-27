package com.tesselslate.pastry.analysis.preemptive;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Processes one or more {@linkplain PreemptiveStronghold strongholds} to
 * provide statistics on their preemptive spikes.
 *
 * @see PreemptiveReading
 */
public class PreemptiveAnalysis {
    public final PreemptiveSpikes valid;
    public final PreemptiveSpikes invalid;

    private final Set<PreemptiveStronghold> strongholds;

    public PreemptiveAnalysis() {
        this.valid = new PreemptiveSpikes();
        this.invalid = new PreemptiveSpikes();

        this.strongholds = new HashSet<>();
    }

    public void add(PreemptiveAnalysis analysis) {
        this.valid.add(analysis.valid);
        this.invalid.add(analysis.invalid);

        this.strongholds.addAll(analysis.strongholds);
    }

    public void process(PreemptiveStronghold stronghold) {
        stronghold.readings.stream().filter(PreemptiveReading::isConsistent).forEach(reading -> {
            boolean isSpike = reading.all(frame -> Arrays.stream(frame.blockEntities())
                    .filter(event -> event.name.equals("mob_spawner") && event.data.equals("silverfish"))
                    .findAny()
                    .isPresent());

            if (isSpike) {
                this.valid.add(reading);
            } else {
                this.invalid.add(reading);
            }
        });

        this.strongholds.add(stronghold);
    }

    public void process(List<PreemptiveStronghold> strongholds) {
        strongholds.forEach(this::process);
    }
}
