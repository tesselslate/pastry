package com.tesselslate.pastry.analysis.preemptive;

import com.tesselslate.pastry.capture.PastryCapture;
import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureBlockEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureBlockOutlineEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureDimensionEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureFrameEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureOptionsEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureProfilerEvent;
import com.tesselslate.pastry.capture.structure.PastryCaptureStructure;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

/**
 * Contains information about preemptive readings obtained by a user from a
 * single stronghold.
 *
 * @see PreemptiveReading
 */
public class PreemptiveStronghold {
    public final PastryCaptureStructure structure;

    public final List<PreemptiveReading> readings;

    /**
     * The constructor is private.
     *
     * @see #readFromCapture(PastryCapture)
     */
    private PreemptiveStronghold(PastryCaptureStructure structure, List<PreemptiveReading> readings) {
        this.structure = structure;
        this.readings = readings;
    }

    /**
     * Reads a list of {@link PreemptiveStronghold} from a {@link PastryCapture}.
     *
     * @return The list of strongholds and their readings present in the capture
     */
    public static List<PreemptiveStronghold> readFromCapture(PastryCapture capture) {
        Set<PastryCaptureStructure> structures = capture.getStructures().stream()
                .filter(structure -> structure.name.equals("stronghold"))
                .collect(Collectors.toSet());

        FrameCollector frameCollector = new FrameCollector();
        capture.getEvents().stream().filter(new OverworldFilter()).forEachOrdered(frameCollector);

        Object2ObjectArrayMap<PastryCaptureStructure, ArrayList<PreemptiveReading>> strongholdMap =
                sortReadings(structures, frameCollector.finish());
        ArrayList<PreemptiveStronghold> strongholds = new ArrayList<>();
        strongholdMap.forEach((structure, readings) -> strongholds.add(new PreemptiveStronghold(structure, readings)));

        return strongholds;
    }

    private static Object2ObjectArrayMap<PastryCaptureStructure, ArrayList<PreemptiveReading>> sortReadings(
            Set<PastryCaptureStructure> structures, List<PreemptiveReading> readings) {
        Object2ObjectArrayMap<PastryCaptureStructure, ArrayList<PreemptiveReading>> strongholds =
                new Object2ObjectArrayMap<>();

        for (PreemptiveReading reading : readings) {
            Optional<PastryCaptureStructure> stronghold = structures.stream()
                    .filter(structure -> {
                        BlockBox bbox = structure.boundingBox;
                        Box box = new Box(bbox.minX, bbox.minY, bbox.minZ, bbox.maxX, bbox.maxY, bbox.maxZ);

                        for (PreemptiveFrame frame : reading.frames()) {
                            if (box.contains(frame.frame().cameraPos)) {
                                return true;
                            }
                        }

                        return false;
                    })
                    .findAny();

            if (stronghold.isPresent()) {
                strongholds
                        .computeIfAbsent(stronghold.get(), structure -> new ArrayList<>())
                        .add(reading);
            }
        }

        return strongholds;
    }

    private static class OverworldFilter implements Predicate<PastryCaptureEvent> {
        private boolean inOverworld = true;

        @Override
        public boolean test(PastryCaptureEvent event) {
            if (event instanceof PastryCaptureDimensionEvent dimensionEvent) {
                inOverworld = dimensionEvent.name.equals("overworld");
            }

            return inOverworld && !(event instanceof PastryCaptureDimensionEvent);
        }
    }

    private static class FrameCollector implements Consumer<PastryCaptureEvent> {
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
                        PastryCaptureBlockEntityEvent[] blockEntities = this.blockEntities.toArray(
                                new PastryCaptureBlockEntityEvent[this.blockEntities.size()]);

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
}
