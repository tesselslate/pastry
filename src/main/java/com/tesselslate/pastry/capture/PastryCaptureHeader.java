package com.tesselslate.pastry.capture;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class PastryCaptureHeader {
    private static final int CURRENT_VERSION = 2;

    public final int version;
    public final int numEvents;

    protected PastryCaptureDictionary dictionary;

    public PastryCaptureHeader(List<PastryCaptureEvent> events) {
        this.version = CURRENT_VERSION;
        this.numEvents = events.size();

        this.dictionary = new PastryCaptureDictionary();
    }

    public PastryCaptureHeader(DataInputStream input) throws IOException {
        this.version = input.readInt();
        this.numEvents = input.readInt();

        this.dictionary = new PastryCaptureDictionary(input);
    }

    public void write(DataOutputStream output) throws IOException {
        output.writeInt(this.version);
        output.writeInt(this.numEvents);

        this.dictionary.write(output);
    }
};
