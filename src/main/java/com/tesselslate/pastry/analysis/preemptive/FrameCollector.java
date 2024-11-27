package com.tesselslate.pastry.analysis.preemptive;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureBlockEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureBlockOutlineEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureFrameEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureOptionsEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureProfilerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reads events from a stream of {@linkplain PastryCaptureEvent capture events},
 * splitting them into lists of events during frames where the piechart was
 * open.
 */
class FrameCollector implements Consumer<PastryCaptureEvent> {
    private ArrayList<PreemptiveReading> readings = new ArrayList<>();
    private ArrayList<PreemptiveFrame> reading = new ArrayList<>();

    private PastryCaptureOptionsEvent options;
    private PastryCaptureProfilerEvent profiler;
    private PastryCaptureBlockOutlineEvent blockOutline;
    private ArrayList<PastryCaptureEntityEvent> entities = new ArrayList<>();
    private ArrayList<PastryCaptureBlockEntityEvent> blockEntities = new ArrayList<>();

    @Override
    public void accept(PastryCaptureEvent event) {
        switch (event) {
            case PastryCaptureFrameEvent frame -> {
                if (this.profiler != null && this.options != null) {
                    PastryCaptureEntityEvent[] entities =
                            this.entities.toArray(new PastryCaptureEntityEvent[this.entities.size()]);
                    PastryCaptureBlockEntityEvent[] blockEntities =
                            this.blockEntities.toArray(new PastryCaptureBlockEntityEvent[this.blockEntities.size()]);

                    this.reading.add(new PreemptiveFrame(
                            frame, this.options, this.profiler, this.blockOutline, entities, blockEntities));
                } else {
                    this.addReading();
                }

                this.options = null;
                this.profiler = null;
                this.blockOutline = null;
                this.entities.clear();
                this.blockEntities.clear();
            }

            case PastryCaptureOptionsEvent e -> this.options = e;
            case PastryCaptureProfilerEvent e -> this.profiler = e;
            case PastryCaptureBlockOutlineEvent e -> this.blockOutline = e;
            case PastryCaptureEntityEvent e -> this.entities.add(e);
            case PastryCaptureBlockEntityEvent e -> this.blockEntities.add(e);

            default -> {
                // Other event types are not processed for preemptive frames.
            }
        }
    }

    public List<PreemptiveReading> finish() {
        this.addReading();

        return this.readings;
    }

    private void addReading() {
        if (this.reading.size() == 0) {
            return;
        }

        this.readings.add(new PreemptiveReading(this.reading.toArray(new PreemptiveFrame[this.reading.size()])));
        this.reading.clear();
    }
}
