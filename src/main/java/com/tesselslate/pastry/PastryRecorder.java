package com.tesselslate.pastry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ProfilerTiming;

public class PastryRecorder {
    private static final String GAMERENDERER_DIRECTORY = "root.gameRenderer.level.entities".replace('.', '\u001e');

    private static final DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private static final int INDEX_BLOCKENTITIES = 0;
    private static final int INDEX_ENTITIES = 1;
    private static final int INDEX_UNSPECIFIED = 2;

    private Object2IntOpenHashMap<String> visibleEntities;
    private Object2IntOpenHashMap<String> visibleBlockEntities;

    private double[] gameRendererTotalPercentages;
    private double[] gameRendererParentPercentages;

    private int frame = 0;
    private boolean dirty = false;
    private int lastWrittenFrame = -1;

    private OutputStream writer;
    private ByteBuffer writeBuffer;

    public PastryRecorder() throws FileNotFoundException, IOException, UnsupportedOperationException {
        // Allocate objects for data collection.
        this.visibleEntities = new Object2IntOpenHashMap<>(107);
        this.visibleBlockEntities = new Object2IntOpenHashMap<>(33);

        this.gameRendererTotalPercentages = new double[3];
        this.gameRendererParentPercentages = new double[3];

        // Open a file writer to record the captured data.
        File outputFile = getOutputFile();
        this.writer = new GZIPOutputStream(new FileOutputStream(outputFile));

        this.writeBuffer = ByteBuffer.allocate(524288);
        if (!this.writeBuffer.hasArray()) {
            throw new UnsupportedOperationException("write buffer does not have backing array");
        }
    }

    public void close() throws IOException {
        if (this.writer == null) {
            return;
        }

        this.writer.close();
        this.writer = null;
    }

    public void recordEntity(Entity entity) {
        this.visibleEntities.addTo(entity.getType().getTranslationKey(), 1);

        this.dirty = true;
    }

    public void recordGlobalBlockEntities(Set<BlockEntity> blockEntities) {
        for (BlockEntity blockEntity : blockEntities) {
            this.recordBlockEntity(blockEntity);
        }

        this.dirty = true;
    }

    public void recordVisibleBlockEntities(Collection<BlockEntity> blockEntities) {
        for (BlockEntity blockEntity : blockEntities) {
            this.recordBlockEntity(blockEntity);
        }

        this.dirty = true;
    }

    public void recordResult(ProfileResult result) {
        this.recordGameRendererResult(result);
        this.writeFrame();
    }

    public void startFrame() {
        this.reset();
        this.frame++;
    }

    private void recordBlockEntity(BlockEntity blockEntity) {
        if (blockEntity instanceof MobSpawnerBlockEntity) {
            MobSpawnerBlockEntity mobSpawner = (MobSpawnerBlockEntity) blockEntity;

            String entityName = mobSpawner.getLogic().getRenderedEntity().getType().getTranslationKey();
            this.visibleBlockEntities.addTo("mob_spawner(" + entityName + ")", 1);
        } else {
            this.visibleBlockEntities.addTo(BlockEntityType.getId(blockEntity.getType()).getPath(), 1);
        }
    }

    private void recordGameRendererResult(ProfileResult result) {
        List<ProfilerTiming> timings = result.getTimings(GAMERENDERER_DIRECTORY);

        for (ProfilerTiming timing : timings) {
            // The `prepare` timing (at least as decompiled) appears completely meaningless
            // and is probably 100% profiler overhead, since it only measures the time to
            // set two variables to 0.

            int index;
            switch (timing.name) {
                case "blockentities":
                    index = INDEX_BLOCKENTITIES;
                    break;
                case "entities":
                    index = INDEX_ENTITIES;
                    break;
                case "unspecified":
                    index = INDEX_UNSPECIFIED;
                    break;
                default:
                    continue;
            }

            this.gameRendererTotalPercentages[index] = timing.totalUsagePercentage;
            this.gameRendererParentPercentages[index] = timing.parentSectionUsagePercentage;
        }

        this.dirty = true;
    }

    private void reset() {
        this.gameRendererTotalPercentages = new double[3];
        this.gameRendererParentPercentages = new double[3];

        this.visibleEntities.clear();
        this.visibleBlockEntities.clear();

        this.dirty = false;
    }

    private void writeFrame() {
        if (!this.dirty || this.writer == null) {
            return;
        }

        if (this.lastWrittenFrame == this.frame) {
            throw new RuntimeException("Attempted to write data for same frame twice");
        }
        this.lastWrittenFrame = this.frame;

        this.writeBuffer.clear();
        this.writeFrameData();
        try {
            this.writer.write(this.writeBuffer.array(), 0, this.writeBuffer.position());
        } catch (IOException e) {
            Pastry.LOGGER.error("Failed to write frame: " + e);
        }
    }

    private void writeFrameData() {
        this.writeBuffer.putInt(this.frame);

        this.writeBuffer.putDouble(this.gameRendererTotalPercentages[0]);
        this.writeBuffer.putDouble(this.gameRendererTotalPercentages[1]);
        this.writeBuffer.putDouble(this.gameRendererTotalPercentages[2]);
        this.writeBuffer.putDouble(this.gameRendererParentPercentages[0]);
        this.writeBuffer.putDouble(this.gameRendererParentPercentages[1]);
        this.writeBuffer.putDouble(this.gameRendererParentPercentages[2]);

        this.writeStringMap(this.visibleEntities);
        this.writeStringMap(this.visibleBlockEntities);
    }

    private void writeStringMap(Object2IntOpenHashMap<String> map) {
        this.writeBuffer.putInt(map.size());

        map.forEach((type, count) -> {
            byte[] stringBytes = type.getBytes();
            this.writeBuffer.putInt(stringBytes.length);
            this.writeBuffer.put(stringBytes);
            this.writeBuffer.putInt(count);
        });
    }

    private static File getOutputFile() {
        File pastryOutputDir = new File(MinecraftClient.getInstance().runDirectory, "pastry-recordings");

        if (!pastryOutputDir.exists()) {
            if (!pastryOutputDir.mkdir()) {
                throw new RuntimeException("Failed to create pastry recordings directory");
            }
        }

        return new File(pastryOutputDir, OUTPUT_DATE_FORMAT.format(new Date()) + ".gz");
    }
}
