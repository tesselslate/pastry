package com.tesselslate.pastry.analysis.preemptive;

import java.util.Arrays;

public class PreemptiveAnalysisResult {
    public final PreemptiveSpikes valid;
    public final PreemptiveSpikes invalid;

    public PreemptiveAnalysisResult() {
        this.valid = new PreemptiveSpikes();
        this.invalid = new PreemptiveSpikes();
    }

    void processStronghold(PreemptiveStronghold stronghold) {
        stronghold.readings.stream().filter(Filters.strictConsistency()).forEach(reading -> {
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
    }
}
