package com.tesselslate.pastry.capture;

import java.io.IOException;

public interface PastryCaptureEvent {
    public PastryCaptureEventType getEventType();

    public void write(PastryCaptureOutputStream output) throws IOException;
}
