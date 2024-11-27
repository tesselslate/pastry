package com.tesselslate.pastry.analysis.preemptive;

import com.tesselslate.pastry.capture.PastryCapture;
import com.tesselslate.pastry.capture.structure.PastryCaptureStructure;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        capture.getEvents().stream().filter(Filters.dimension("overworld")).forEachOrdered(frameCollector);

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
}
