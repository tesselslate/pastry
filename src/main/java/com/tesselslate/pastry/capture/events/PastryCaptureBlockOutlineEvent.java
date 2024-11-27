package com.tesselslate.pastry.capture.events;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a targeted block outline was drawn on this frame.
 *
 * @since format V3
 */
public class PastryCaptureBlockOutlineEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.BLOCK_OUTLINE;

    /**
     * The position of the targeted block in the world.
     */
    @NotNull
    public final BlockPos blockPos;

    public PastryCaptureBlockOutlineEvent(@NotNull BlockPos blockPos) {
        Objects.requireNonNull(blockPos);

        this.blockPos = blockPos;
    }

    public PastryCaptureBlockOutlineEvent(PastryCaptureInputStream input) throws IOException {
        long packedPos = input.readLong();

        this.blockPos = new BlockPos(
                BlockPos.unpackLongX(packedPos), BlockPos.unpackLongY(packedPos), BlockPos.unpackLongZ(packedPos));
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        long packedPos = this.blockPos.asLong();

        output.writeLong(packedPos);
    }

    @Override
    public int hashCode() {
        return this.blockPos.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PastryCaptureBlockOutlineEvent)) {
            return false;
        } else {
            PastryCaptureBlockOutlineEvent other = (PastryCaptureBlockOutlineEvent) obj;

            return this.blockPos.equals(other.blockPos);
        }
    }
}
