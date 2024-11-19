package com.tesselslate.pastry.task;

import java.util.List;
import java.util.concurrent.RecursiveTask;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveAnalysis;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveStronghold;
import com.tesselslate.pastry.capture.PastryCapture;

/**
 * Reads and analyzes a single {@link PastryCapture}.
 */
public class AnalyzeCaptureTask extends RecursiveTask<Exceptional<PreemptiveAnalysis>> {
    private final PastryCapture capture;

    public AnalyzeCaptureTask(PastryCapture capture) {
        this.capture = capture;
    }

    @Override
    protected Exceptional<PreemptiveAnalysis> compute() {
        try {
            List<PreemptiveStronghold> strongholds = PreemptiveStronghold.readFromCapture(capture);

            PreemptiveAnalysis analysis = new PreemptiveAnalysis();
            strongholds.forEach(stronghold -> analysis.process(stronghold));
            return new Exceptional<>(analysis);
        } catch (Exception e) {
            return new Exceptional<>(e);
        }
    }
}
