package com.tesselslate.pastry.capture;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Contains the string lookup table and other critical metadata for a Pastry
 * recording.
 */
public class PastryCaptureHeader {
    private static final int CURRENT_VERSION = 8;

    public final int version;
    public final int numEvents;

    /**
     * @since format V8
     */
    public final Date recordedAt;

    protected PastryCaptureDictionary dictionary;

    public PastryCaptureHeader(List<PastryCaptureEvent> events) {
        this.version = CURRENT_VERSION;
        this.numEvents = events.size();
        this.recordedAt = new Date();

        this.dictionary = new PastryCaptureDictionary();
    }

    public PastryCaptureHeader(DataInputStream input) throws IOException {
        this.version = input.readInt();
        this.numEvents = input.readInt();
        this.recordedAt = new Date(input.readLong());

        this.dictionary = new PastryCaptureDictionary(input);
    }

    /**
     * Serializes and writes the header to {@code output}.
     *
     * @param output The stream to which the header is written
     */
    public void write(DataOutputStream output) throws IOException {
        output.writeInt(this.version);
        output.writeInt(this.numEvents);
        output.writeLong(this.recordedAt.getTime());

        this.dictionary.write(output);
    }
};
