package com.tesselslate.pastry.capture;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.tesselslate.pastry.capture.events.PastryCaptureBlockEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureFrameEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureWorldLoadEvent;

/**
 * Provides an abstraction over the string lookup table and allows for reading
 * an entire capture from an {@link InputStream}.
 *
 * @see PastryCaptureDictionary
 */
public class PastryCaptureInputStream extends DataInputStream {
    private PastryCaptureHeader header;

    public PastryCaptureInputStream(InputStream input) throws IOException {
        super(input);

        this.header = new PastryCaptureHeader(new DataInputStream(input));
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
            }
        }

        return Arrays.asList(events);
    }
}
