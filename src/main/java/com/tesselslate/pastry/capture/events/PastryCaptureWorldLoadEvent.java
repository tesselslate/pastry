package com.tesselslate.pastry.capture.events;

import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.world.SaveProperties;

/**
 * Contains information about the world which this recording was taken in. This
 * event is only present if the recording was taken on an integrated server
 * world.
 *
 * @since format V2
 */
public class PastryCaptureWorldLoadEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.WORLD_LOAD;

    @NotNull
    public final String worldName;
    public final long worldSeed;

    public PastryCaptureWorldLoadEvent(SaveProperties properties) {
        this.worldName = properties.getLevelName();
        Objects.requireNonNull(this.worldName);

        this.worldSeed = properties.getGeneratorOptions().getSeed();
    }

    public PastryCaptureWorldLoadEvent(PastryCaptureInputStream input) throws IOException {
        this.worldName = input.readString();
        Objects.requireNonNull(this.worldName);

        this.worldSeed = input.readLong();
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeString(this.worldName);
        output.writeLong(this.worldSeed);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(this.worldName);
        result = prime * result + (int) (this.worldSeed ^ (this.worldSeed >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PastryCaptureWorldLoadEvent)) {
            return false;
        } else {
            PastryCaptureWorldLoadEvent other = (PastryCaptureWorldLoadEvent) obj;

            return (this.worldName.equals(other.worldName)) && (this.worldSeed == other.worldSeed);
        }
    }
}
