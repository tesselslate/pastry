package com.tesselslate.pastry.capture.events;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ProfilerTiming;

/**
 * Contains information about a single rendered frame during which the pie chart
 * was visible and the game was unpaused.
 *
 * @since format V1
 */
public class PastryCaptureFrameEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.FRAME;
    private static final String GAMERENDERER_DIRECTORY = "root.gameRenderer.level.entities".replace('.', '\u001e');

    /**
     * A unique number which identifies this frame. The frame counter is
     * monotonically increasing and increments every time a frame is drawn.
     */
    public int frameNumber;
    public Vec3d cameraPos;
    public float pitch, yaw;

    public float blockEntityPercentage;
    public float entityPercentage;
    public float unspecifiedPercentage;

    public PastryCaptureFrameEvent(int frameNumber, Vec3d cameraPos, float pitch, float yaw,
            ProfileResult profileResult) {
        this.frameNumber = frameNumber;
        this.cameraPos = cameraPos;
        this.pitch = pitch;
        this.yaw = yaw;

        List<ProfilerTiming> timings = profileResult.getTimings(GAMERENDERER_DIRECTORY);
        for (ProfilerTiming timing : timings) {
            switch (timing.name) {
                case "blockentities":
                    this.blockEntityPercentage = (float) timing.parentSectionUsagePercentage;
                    break;
                case "entities":
                    this.entityPercentage = (float) timing.parentSectionUsagePercentage;
                    break;
                case "unspecified":
                    this.unspecifiedPercentage = (float) timing.parentSectionUsagePercentage;
                    break;
            }
        }
    }

    public PastryCaptureFrameEvent(PastryCaptureInputStream input) throws IOException {
        this.frameNumber = input.readInt();

        double playerX = input.readDouble();
        double playerY = input.readDouble();
        double playerZ = input.readDouble();
        this.cameraPos = new Vec3d(playerX, playerY, playerZ);

        this.pitch = input.readFloat();
        this.yaw = input.readFloat();

        this.blockEntityPercentage = readPercentage(input);
        this.entityPercentage = readPercentage(input);
        this.unspecifiedPercentage = readPercentage(input);
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeInt(this.frameNumber);

        output.writeDouble(this.cameraPos.x);
        output.writeDouble(this.cameraPos.y);
        output.writeDouble(this.cameraPos.z);

        output.writeFloat(this.pitch);
        output.writeFloat(this.yaw);

        writePercentage(output, this.blockEntityPercentage);
        writePercentage(output, this.entityPercentage);
        writePercentage(output, this.unspecifiedPercentage);
    }

    private static float readPercentage(DataInputStream input) throws IOException {
        byte whole = input.readByte();
        byte decimal = input.readByte();

        return (float) whole + ((float) decimal / 100.0f);
    }

    private static void writePercentage(PastryCaptureOutputStream output, float percentage) throws IOException {
        byte whole = (byte) Math.floor(percentage);
        byte decimal = (byte) Math.floor((percentage % 1.0f) * 100.0f);

        output.writeByte(whole);
        output.writeByte(decimal);
    }
}
