package com.tesselslate.pastry.analysis.preemptive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Processes one or more {@linkplain PreemptiveStronghold strongholds} to
 * provide statistics on their preemptive spikes.
 *
 * @see PreemptiveReading
 */
public class PreemptiveAnalysis {
    private final List<PreemptiveStronghold> strongholds;

    public PreemptiveAnalysis() {
        this.strongholds = new ArrayList<>();
    }

    public void add(PreemptiveAnalysis analysis) {
        this.strongholds.addAll(analysis.strongholds);
    }

    public void add(PreemptiveStronghold stronghold) {
        this.strongholds.add(stronghold);
    }

    public void add(Collection<PreemptiveStronghold> strongholds) {
        this.strongholds.addAll(strongholds);
    }

    public PreemptiveAnalysisResult process() {
        PreemptiveAnalysisResult result = new PreemptiveAnalysisResult();
        this.strongholds.forEach(stronghold -> result.processStronghold(stronghold));

        return result;
    }
}
