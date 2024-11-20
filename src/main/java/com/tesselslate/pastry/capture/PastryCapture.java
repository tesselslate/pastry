package com.tesselslate.pastry.capture;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.tesselslate.pastry.capture.structure.PastryCaptureStructure;

/**
 * Contains a series of capture events which have been generated by the player
 * or read from an on-disk recording.
 */
public class PastryCapture {
    private List<PastryCaptureEvent> events;

    private Set<PastryCaptureStructure> structures;

    private ArrayList<PastryCaptureEvent> queuedEvents;

    public PastryCapture() {
        this.events = new ArrayList<>();
        this.structures = new HashSet<>();
        this.queuedEvents = new ArrayList<>();
    }

    public PastryCapture(GZIPInputStream input)
            throws IndexOutOfBoundsException, IOException, PastryCaptureVersionException {
        PastryCaptureInputStream pastryInput = new PastryCaptureInputStream(input);

        this.structures = pastryInput.readAllStructures();
        this.events = pastryInput.readAllEvents();
    }

    /**
     * Adds an event to the list of recorded events.
     *
     * @param event The event to add
     * @throws UnsupportedOperationException If the capture was read from an input
     *                                       stream
     */
    public void add(PastryCaptureEvent event) throws UnsupportedOperationException {
        this.events.add(event);
    }

    /**
     * Adds a structure to the list of recorded structures if it is not already
     * present.
     *
     * @param structure The structure to add
     * @throws UnsupportedOperationException If the capture was read from an input
     *                                       stream
     */
    public void addStructure(PastryCaptureStructure structure) throws UnsupportedOperationException {
        this.structures.add(structure);
    }

    /**
     * Adds all queued events to the list of recorded events.
     *
     * @throws UnsupportedOperationException If the capture was read from an input
     *                                       stream
     */
    public void addQueued() throws UnsupportedOperationException {
        if (this.queuedEvents == null) {
            throw new UnsupportedOperationException("Cannot add queued events on read-only PastryCapture");
        }

        this.events.addAll(this.queuedEvents);
        this.queuedEvents.clear();
    }

    /**
     * Clears all queued events without adding them to the list of recorded
     * events.
     *
     * @throws UnsupportedOperationException If the capture was read from an input
     *                                       stream
     */
    public void clearQueue() throws UnsupportedOperationException {
        if (this.queuedEvents == null) {
            throw new UnsupportedOperationException("Cannot clear queued events on read-only PastryCapture");
        }

        this.queuedEvents.clear();
    }

    /**
     * Returns the number of events contained in the capture.
     *
     * @return The number of events in this capture
     */
    public int size() {
        return this.events.size();
    }

    /**
     * Adds an event to the list of queued events.
     *
     * @param event The event to add to the queue
     * @throws UnsupportedOperationException If the capture was read from an input
     *                                       stream
     */
    public void queue(PastryCaptureEvent event) throws UnsupportedOperationException {
        if (this.queuedEvents == null) {
            throw new UnsupportedOperationException("Cannot queue events on read-only PastryCapture");
        }

        this.queuedEvents.add(event);
    }

    /**
     * Serializes and writes a capture header and the list of recorded events to
     * {@code output}.
     *
     * @param output The output stream to which the capture is written
     */
    public void writeTo(OutputStream output) throws IOException {
        try (PastryCaptureOutputStream pastryOutput = new PastryCaptureOutputStream(output, this.events)) {
            pastryOutput.writeStructures(this.structures);
            pastryOutput.writeEvents(this.events);
        }
    }

    /**
     * Returns the list of all events contained in this capture.
     *
     * @returns All events in this capture
     */
    public List<PastryCaptureEvent> getEvents() {
        return this.events;
    }

    /**
     * Returns the set of all structures contained in this capture.
     *
     * @returns All structures in this capture
     */
    public Set<PastryCaptureStructure> getStructures() {
        return this.structures;
    }
}
