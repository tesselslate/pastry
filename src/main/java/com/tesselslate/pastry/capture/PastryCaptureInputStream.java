package com.tesselslate.pastry.capture;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.tesselslate.pastry.capture.events.PastryCaptureBlockEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureBlockOutlineEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureDimensionEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureFrameEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureGamemodeEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureOptionsEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureProfilerEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureSysinfoEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureWorldLoadEvent;
import com.tesselslate.pastry.capture.structure.PastryCaptureStructure;

import net.minecraft.util.math.BlockBox;

/**
 * Provides an abstraction over the string lookup table and allows for reading
 * an entire capture from an {@link InputStream}.
 *
 * @see PastryCaptureDictionary
 */
public class PastryCaptureInputStream extends DataInputStream {
    private PastryCaptureHeader header;

    public PastryCaptureInputStream(InputStream input) throws IOException, PastryCaptureVersionException {
        super(input);

        this.header = new PastryCaptureHeader(new DataInputStream(input));
    }

    /**
     * Reads a {@link BlockBox} from the input stream.
     *
     * @return The deserialized block box read from the input stream
     */
    public BlockBox readBlockBox() throws IOException {
        int minX = super.readInt();
        int minY = super.readInt();
        int minZ = super.readInt();
        int maxX = super.readInt();
        int maxY = super.readInt();
        int maxZ = super.readInt();

        return new BlockBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Attempts to read an ID from the input stream and retrieves the
     * corresponding string from the lookup table.
     *
     * @return The string pointed to by the read ID, or null if the ID is 0
     * @throws IndexOutOfBoundsException If the read ID is invalid
     */
    public @Nullable String readString() throws IndexOutOfBoundsException, IOException {
        int id = super.readInt();
        if (id == 0) {
            return null;
        }

        return this.header.dictionary.get(id);
    }

    /**
     * Attempts to read the entire list of capture events from the input stream.
     *
     * @return The list of all capture events
     */
    public List<PastryCaptureEvent> readAllEvents() throws IndexOutOfBoundsException, IOException {
        PastryCaptureEvent[] events = new PastryCaptureEvent[this.header.numEvents];

        for (int i = 0; i < events.length; i++) {
            PastryCaptureEventType eventType = PastryCaptureEventType.fromInt(super.readInt());

            switch (eventType) {
                case FRAME:
                    events[i] = new PastryCaptureFrameEvent(this);
                    break;
                case ENTITY:
                    events[i] = new PastryCaptureEntityEvent(this);
                    break;
                case BLOCKENTITY:
                    events[i] = new PastryCaptureBlockEntityEvent(this);
                    break;
                case WORLD_LOAD:
                    events[i] = new PastryCaptureWorldLoadEvent(this);
                    break;
                case BLOCK_OUTLINE:
                    events[i] = new PastryCaptureBlockOutlineEvent(this);
                    break;
                case OPTIONS:
                    events[i] = new PastryCaptureOptionsEvent(this);
                    break;
                case DIMENSION:
                    events[i] = new PastryCaptureDimensionEvent(this);
                    break;
                case SYSINFO:
                    events[i] = new PastryCaptureSysinfoEvent(this);
                    break;
                case PROFILER:
                    events[i] = new PastryCaptureProfilerEvent(this);
                    break;
                case GAMEMODE:
                    events[i] = new PastryCaptureGamemodeEvent(this);
                    break;
            }
        }

        return Arrays.asList(events);
    }

    /**
     * Attempts to read the entire list of structures from the input stream as
     * a set.
     *
     * @return The set of all structures
     */
    public Set<PastryCaptureStructure> readAllStructures() throws IOException {
        HashSet<PastryCaptureStructure> structures = new HashSet<>();

        int size = super.readInt();

        for (int i = 0; i < size; i++) {
            structures.add(new PastryCaptureStructure(this));
        }

        return structures;
    }
}
