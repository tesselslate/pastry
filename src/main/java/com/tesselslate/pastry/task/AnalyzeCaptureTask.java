package com.tesselslate.pastry.task;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveAnalysis;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveStronghold;
import com.tesselslate.pastry.capture.PastryCapture;

import java.util.List;

/**
 * Reads and analyzes a single {@link PastryCapture}.
 */
public class AnalyzeCaptureTask extends PastryTask<PreemptiveAnalysis> {
    private final PastryCapture capture;

    public AnalyzeCaptureTask(PastryCapture capture) {
        super("AnalyzeCaptureTask");

        this.capture = capture;
    }

    @Override
    protected PreemptiveAnalysis runTask() {
        List<PreemptiveStronghold> strongholds = PreemptiveStronghold.readFromCapture(capture);

        PreemptiveAnalysis analysis = new PreemptiveAnalysis();
        strongholds.forEach(stronghold -> analysis.add(stronghold));
        return analysis;
    }
}
