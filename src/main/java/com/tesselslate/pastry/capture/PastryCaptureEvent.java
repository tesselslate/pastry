package com.tesselslate.pastry.capture;

import java.io.IOException;

/**
 * Represents a single event containing arbitrary data which may be written to
 * and read from a Pastry recording.
 */
public interface PastryCaptureEvent {
    public PastryCaptureEventType getEventType();

    /**
     * Serializes and writes the contents of the event to output.
     *
     * @param output The output stream to which the serialized event is written.
     */
    public void write(PastryCaptureOutputStream output) throws IOException;
}
