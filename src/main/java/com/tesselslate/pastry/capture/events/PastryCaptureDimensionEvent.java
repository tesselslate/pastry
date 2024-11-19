package com.tesselslate.pastry.capture.events;

import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public final String name;

    public PastryCaptureDimensionEvent(ClientWorld world) {
        this.name = world.getDimensionRegistryKey().getValue().getPath();
        Objects.requireNonNull(this.name);
    }

    public PastryCaptureDimensionEvent(PastryCaptureInputStream input) throws IOException {
        this.name = input.readString();
        Objects.requireNonNull(this.name);
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeString(this.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PastryCaptureDimensionEvent)) {
            return false;
        } else {
            PastryCaptureDimensionEvent other = (PastryCaptureDimensionEvent) obj;

            return this.name.equals(other.name);
        }
    }
}
