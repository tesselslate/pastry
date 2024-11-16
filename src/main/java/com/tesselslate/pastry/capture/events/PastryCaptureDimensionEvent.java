package com.tesselslate.pastry.capture.events;

import java.io.IOException;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.client.world.ClientWorld;

/**
 * Contains information about the player entering a dimension.
 *
 * @since format V6
 */
public class PastryCaptureDimensionEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.DIMENSION;

    public String name;

    public PastryCaptureDimensionEvent(ClientWorld world) {
        this.name = world.getDimensionRegistryKey().getValue().getPath();
    }

    public PastryCaptureDimensionEvent(PastryCaptureInputStream input) throws IOException {
        this.name = input.readString();
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeString(this.name);
    }

}
