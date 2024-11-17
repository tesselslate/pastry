package com.tesselslate.pastry.capture.events;

import java.io.IOException;

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

    public int time;
    public Vec3d cameraPos;
    public float pitch, yaw;

    public PastryCaptureFrameEvent(int time, Camera camera) {
        this.time = time;

        this.cameraPos = camera.getPos();
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
}
