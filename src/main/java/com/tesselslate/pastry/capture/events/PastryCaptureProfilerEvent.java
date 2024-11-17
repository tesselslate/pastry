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

    public float blockEntityPercentage;
    public float entityPercentage;
    public float unspecifiedPercentage;

    public PastryCaptureProfilerEvent(ProfileResult profileResult) {
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
}
