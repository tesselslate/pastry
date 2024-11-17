package com.tesselslate.pastry.capture.events;

import java.io.IOException;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.world.GameMode;

/**
 * Contains the current gamemode of the player.
 *
 * @since format V10
 */
public class PastryCaptureGamemodeEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.GAMEMODE;

    public GameMode mode;

    public PastryCaptureGamemodeEvent(GameMode mode) {
        this.mode = mode;
    }

    public PastryCaptureGamemodeEvent(PastryCaptureInputStream input) throws IOException {
        this.mode = GameMode.byId(input.readByte());
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeByte(this.mode.getId());
    }
}
