package com.tesselslate.pastry.capture.events;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.jetbrains.annotations.Nullable;

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

    public final float blockEntityParentPercentage;
    public final float entityParentPercentage;
    public final float unspecifiedParentPercentage;
    public final float destroyProgressParentPercentage;
    public final float prepareParentPercentage;

    public final float blockEntityTotalPercentage;
    public final float entityTotalPercentage;
    public final float unspecifiedTotalPercentage;
    public final float destroyProgressTotalPercentage;
    public final float prepareTotalPercentage;

    public final float totalPercentage;

    public PastryCaptureProfilerEvent(ProfileResult profileResult) {
        ProfilerTiming blockEntity = null;
        ProfilerTiming entity = null;
        ProfilerTiming unspecified = null;
        ProfilerTiming destroyProgress = null;
        ProfilerTiming prepare = null;

        List<ProfilerTiming> timings = profileResult.getTimings(GAMERENDERER_DIRECTORY);
        for (ProfilerTiming timing : timings) {
            switch (timing.name) {
                case "blockentities":
                    blockEntity = timing;
                    break;
                case "entities":
                    entity = timing;
                    break;
                case "unspecified":
                    unspecified = timing;
                    break;
                case "destroyProgress":
                    destroyProgress = timing;
                    break;
                case "prepare":
                    prepare = timing;
                    break;
            }
        }

        this.blockEntityParentPercentage = getParent(blockEntity);
        this.entityParentPercentage = getParent(entity);
        this.unspecifiedParentPercentage = getParent(unspecified);
        this.destroyProgressParentPercentage = getParent(destroyProgress);
        this.prepareParentPercentage = getParent(prepare);

        this.blockEntityTotalPercentage = getTotal(blockEntity);
        this.entityTotalPercentage = getTotal(entity);
        this.unspecifiedTotalPercentage = getTotal(unspecified);
        this.destroyProgressTotalPercentage = getTotal(destroyProgress);
        this.prepareTotalPercentage = getTotal(prepare);

        this.totalPercentage = (float) timings.get(0).totalUsagePercentage;
    }

    public PastryCaptureProfilerEvent(PastryCaptureInputStream input) throws IOException {
        this.blockEntityParentPercentage = readPercentage(input);
        this.entityParentPercentage = readPercentage(input);
        this.unspecifiedParentPercentage = readPercentage(input);
        this.destroyProgressParentPercentage = readPercentage(input);
        this.prepareParentPercentage = readPercentage(input);

        this.blockEntityTotalPercentage = readPercentage(input);
        this.entityTotalPercentage = readPercentage(input);
        this.unspecifiedTotalPercentage = readPercentage(input);
        this.destroyProgressTotalPercentage = readPercentage(input);
        this.prepareTotalPercentage = readPercentage(input);

        this.totalPercentage = readPercentage(input);
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        writePercentage(output, this.blockEntityParentPercentage);
        writePercentage(output, this.entityParentPercentage);
        writePercentage(output, this.unspecifiedParentPercentage);
        writePercentage(output, this.destroyProgressParentPercentage);
        writePercentage(output, this.prepareParentPercentage);

        writePercentage(output, this.blockEntityTotalPercentage);
        writePercentage(output, this.entityTotalPercentage);
        writePercentage(output, this.unspecifiedTotalPercentage);
        writePercentage(output, this.destroyProgressTotalPercentage);
        writePercentage(output, this.prepareTotalPercentage);

        writePercentage(output, this.totalPercentage);
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
        result = prime * result + Float.floatToIntBits(this.blockEntityParentPercentage);
        result = prime * result + Float.floatToIntBits(this.entityParentPercentage);
        result = prime * result + Float.floatToIntBits(this.unspecifiedParentPercentage);
        result = prime * result + Float.floatToIntBits(this.destroyProgressParentPercentage);
        result = prime * result + Float.floatToIntBits(this.prepareParentPercentage);
        result = prime * result + Float.floatToIntBits(this.blockEntityTotalPercentage);
        result = prime * result + Float.floatToIntBits(this.entityTotalPercentage);
        result = prime * result + Float.floatToIntBits(this.unspecifiedTotalPercentage);
        result = prime * result + Float.floatToIntBits(this.destroyProgressTotalPercentage);
        result = prime * result + Float.floatToIntBits(this.prepareTotalPercentage);
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

            return (this.blockEntityParentPercentage == other.blockEntityParentPercentage)
                    && (this.entityParentPercentage == other.entityParentPercentage)
                    && (this.unspecifiedParentPercentage == other.unspecifiedParentPercentage)
                    && (this.destroyProgressParentPercentage == other.destroyProgressParentPercentage)
                    && (this.prepareParentPercentage == other.prepareParentPercentage)
                    && (this.blockEntityTotalPercentage == other.blockEntityTotalPercentage)
                    && (this.entityTotalPercentage == other.entityTotalPercentage)
                    && (this.unspecifiedTotalPercentage == other.unspecifiedTotalPercentage)
                    && (this.destroyProgressTotalPercentage == other.destroyProgressTotalPercentage)
                    && (this.prepareTotalPercentage == other.prepareTotalPercentage);
        }
    }

    private static float getParent(@Nullable ProfilerTiming timing) {
        return timing != null ? (float) timing.parentSectionUsagePercentage : 0f;
    }

    private static float getTotal(@Nullable ProfilerTiming timing) {
        return timing != null ? (float) timing.totalUsagePercentage : 0f;
    }
}
