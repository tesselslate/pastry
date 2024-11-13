package com.tesselslate.pastry.capture.events;

import java.io.IOException;

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

    public String worldName;
    public long worldSeed;

    public PastryCaptureWorldLoadEvent(SaveProperties properties) {
        this.worldName = properties.getLevelName();
        this.worldSeed = properties.getGeneratorOptions().getSeed();
    }

    public PastryCaptureWorldLoadEvent(PastryCaptureInputStream input) throws IOException {
        this.worldName = input.readString();
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
}
