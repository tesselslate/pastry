package com.tesselslate.pastry.capture;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class PastryCapture {
    private List<PastryCaptureEvent> events;

    private ArrayList<PastryCaptureEvent> queuedEvents;

    public PastryCapture() {
        this.events = new ArrayList<>();

        this.queuedEvents = new ArrayList<>();
    }

    public PastryCapture(GZIPInputStream input) throws IndexOutOfBoundsException, IOException {
        PastryCaptureInputStream pastryInput = new PastryCaptureInputStream(input);

        this.events = pastryInput.readAllEvents();
    }

    public void add(PastryCaptureEvent event) throws UnsupportedOperationException {
        this.events.add(event);
    }

    public void addQueued() throws UnsupportedOperationException {
        if (this.queuedEvents == null) {
            throw new UnsupportedOperationException("Cannot add queued events on read-only PastryCapture");
        }

        this.events.addAll(this.queuedEvents);
        this.queuedEvents.clear();
    }

    public void clearQueue() throws UnsupportedOperationException {
        if (this.queuedEvents == null) {
            throw new UnsupportedOperationException("Cannot clear queued events on read-only PastryCapture");
        }

        this.queuedEvents.clear();
    }

    public int size() {
        return this.events.size();
    }

    public void queue(PastryCaptureEvent event) throws UnsupportedOperationException {
        if (this.queuedEvents == null) {
            throw new UnsupportedOperationException("Cannot queue events on read-only PastryCapture");
        }

        this.queuedEvents.add(event);
    }

    public void writeTo(OutputStream output) throws IOException {
        try (PastryCaptureOutputStream pastryOutput = new PastryCaptureOutputStream(output, this.events)) {
            for (PastryCaptureEvent event : this.events) {
                pastryOutput.writeInt(event.getEventType().ordinal());
                event.write(pastryOutput);
            }
        }
    }
}
