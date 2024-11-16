package com.tesselslate.pastry.capture;

/**
 * Represents a failure to read a Pastry capture due to its reported version.
 */
public class PastryCaptureVersionException extends Exception {
    public int version;

    public PastryCaptureVersionException(int version) {
        super("Cannot read capture of version " + version);

        this.version = version;
    }

    public PastryCaptureVersionException(int version, String message) {
        super("Cannot read capture of version " + version + ": " + message);

        this.version = version;
    }
}
