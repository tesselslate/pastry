package com.tesselslate.pastry.capture.events;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ProfilerTiming;

/**
 * Contains information about the gameRenderer profiler results for a single
 * frame.
 *
 * @since format V9
 */
public class PastryCaptureProfilerEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.PROFILER;

    private static final String GAMERENDERER_DIRECTORY = "root.gameRenderer.level.entities".replace('.', '\u001e');

    public final float blockEntityPercentage;
    public final float entityPercentage;
    public final float unspecifiedPercentage;

    public PastryCaptureProfilerEvent(ProfileResult profileResult) {
        float blockEntityPercentage = 0;
        float entityPercentage = 0;
        float unspecifiedPercentage = 0;

        List<ProfilerTiming> timings = profileResult.getTimings(GAMERENDERER_DIRECTORY);
        for (ProfilerTiming timing : timings) {
            switch (timing.name) {
                case "blockentities":
                    blockEntityPercentage = (float) timing.parentSectionUsagePercentage;
                    break;
                case "entities":
                    entityPercentage = (float) timing.parentSectionUsagePercentage;
                    break;
                case "unspecified":
                    unspecifiedPercentage = (float) timing.parentSectionUsagePercentage;
                    break;
            }
        }

        this.blockEntityPercentage = blockEntityPercentage;
        this.entityPercentage = entityPercentage;
        this.unspecifiedPercentage = unspecifiedPercentage;
    }

    public PastryCaptureProfilerEvent(PastryCaptureInputStream input) throws IOException {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(this.blockEntityPercentage);
        result = prime * result + Float.floatToIntBits(this.entityPercentage);
        result = prime * result + Float.floatToIntBits(this.unspecifiedPercentage);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PastryCaptureProfilerEvent)) {
            return false;
        } else {
            PastryCaptureProfilerEvent other = (PastryCaptureProfilerEvent) obj;

            return (this.blockEntityPercentage == other.blockEntityPercentage)
                    && (this.entityPercentage == other.entityPercentage)
                    && (this.unspecifiedPercentage == other.unspecifiedPercentage);
        }
    }
}
