package com.tesselslate.pastry.capture.events;

import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

/**
 * Contains information about a single rendered frame during which the game was
 * unpaused.
 *
 * @since format V1
 */
public class PastryCaptureFrameEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.FRAME;

    public final int time;

    @NotNull
    public final Vec3d cameraPos;

    public final float pitch, yaw;

    public PastryCaptureFrameEvent(int time, Camera camera) {
        this.time = time;

        this.cameraPos = camera.getPos();
        Objects.requireNonNull(this.cameraPos);

        this.pitch = camera.getPitch();
        this.yaw = camera.getYaw();
    }

    public PastryCaptureFrameEvent(PastryCaptureInputStream input) throws IOException {
        this.time = input.readInt();

        double playerX = input.readDouble();
        double playerY = input.readDouble();
        double playerZ = input.readDouble();
        this.cameraPos = new Vec3d(playerX, playerY, playerZ);

        this.pitch = input.readFloat();
        this.yaw = input.readFloat();
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeInt(this.time);

        output.writeDouble(this.cameraPos.x);
        output.writeDouble(this.cameraPos.y);
        output.writeDouble(this.cameraPos.z);

        output.writeFloat(this.pitch);
        output.writeFloat(this.yaw);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.time;
        result = prime * result + Objects.hashCode(this.cameraPos);
        result = prime * result + Float.floatToIntBits(this.pitch);
        result = prime * result + Float.floatToIntBits(this.yaw);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PastryCaptureFrameEvent)) {
            return false;
        } else {
            PastryCaptureFrameEvent other = (PastryCaptureFrameEvent) obj;

            return (this.time == other.time) && (this.cameraPos.equals(other.cameraPos)) && (this.pitch == other.pitch)
                    && (this.yaw == other.yaw);
        }
    }
}
