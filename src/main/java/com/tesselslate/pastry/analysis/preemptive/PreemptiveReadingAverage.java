package com.tesselslate.pastry.analysis.preemptive;

import com.tesselslate.pastry.capture.events.PastryCaptureProfilerEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PreemptiveReadingAverage {
    public final float blockEntityParentPercentage;
    public final float entityParentPercentage;
    public final float unspecifiedParentPercentage;
    public final float destroyProgressParentPercentage;
    public final float prepareParentPercentage;
    public final float blockEntityTotalPercentage;
    public final float entityTotalPercentage;
    public final float unspecifiedTotalPercentage;
    public final float destroyProgressTotalPercentage;
    public final float prepareTotalPercentage;
    public final float totalPercentage;

    public PreemptiveReadingAverage(PreemptiveReading reading) {
        this(List.of(reading));
    }

    public PreemptiveReadingAverage(PreemptiveSpikes spikes) {
        this(spikes.getReadings());
    }

    private PreemptiveReadingAverage(Collection<PreemptiveReading> readings) {
        double blockEntityParentPercentage = 0;
        double entityParentPercentage = 0;
        double unspecifiedParentPercentage = 0;
        double destroyProgressParentPercentage = 0;
        double prepareParentPercentage = 0;
        double blockEntityTotalPercentage = 0;
        double entityTotalPercentage = 0;
        double unspecifiedTotalPercentage = 0;
        double destroyProgressTotalPercentage = 0;
        double prepareTotalPercentage = 0;
        double totalPercentage = 0;

        List<PreemptiveFrame> frames = readings.stream()
                .flatMap(reading -> Arrays.stream(reading.frames()))
                .collect(Collectors.toList());

        int frameCount = 0;
        for (PreemptiveFrame frame : frames) {
            PastryCaptureProfilerEvent event = frame.profiler();
            frameCount++;

            blockEntityParentPercentage += (double) event.blockEntityParentPercentage;
            entityParentPercentage += (double) event.entityParentPercentage;
            unspecifiedParentPercentage += (double) event.unspecifiedParentPercentage;
            destroyProgressParentPercentage += (double) event.destroyProgressParentPercentage;
            prepareParentPercentage += (double) event.prepareParentPercentage;
            blockEntityTotalPercentage += (double) event.blockEntityTotalPercentage;
            entityTotalPercentage += (double) event.entityTotalPercentage;
            unspecifiedTotalPercentage += (double) event.unspecifiedTotalPercentage;
            destroyProgressTotalPercentage += (double) event.destroyProgressTotalPercentage;
            prepareTotalPercentage += (double) event.prepareTotalPercentage;
            totalPercentage += (double) event.totalPercentage;
        }

        this.blockEntityParentPercentage = (float) (blockEntityParentPercentage / (double) frameCount);
        this.entityParentPercentage = (float) (entityParentPercentage / (double) frameCount);
        this.unspecifiedParentPercentage = (float) (unspecifiedParentPercentage / (double) frameCount);
        this.destroyProgressParentPercentage = (float) (destroyProgressParentPercentage / (double) frameCount);
        this.prepareParentPercentage = (float) (prepareParentPercentage / (double) frameCount);
        this.blockEntityTotalPercentage = (float) (blockEntityTotalPercentage / (double) frameCount);
        this.entityTotalPercentage = (float) (entityTotalPercentage / (double) frameCount);
        this.unspecifiedTotalPercentage = (float) (unspecifiedTotalPercentage / (double) frameCount);
        this.destroyProgressTotalPercentage = (float) (destroyProgressTotalPercentage / (double) frameCount);
        this.prepareTotalPercentage = (float) (prepareTotalPercentage / (double) frameCount);
        this.totalPercentage = (float) (totalPercentage / (double) frameCount);
    }
}
