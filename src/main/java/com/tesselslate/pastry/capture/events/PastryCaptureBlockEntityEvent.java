package com.tesselslate.pastry.capture.events;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

/**
 * Contains information about a single visible block entity.
 *
 * @since format V1
 */
public class PastryCaptureBlockEntityEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.BLOCKENTITY;

    /**
     * The world coordinates of the block entity.
     */
    public Vec3i pos;

    /**
     * The ID of the block entity (namespace not included.)
     */
    public String name;

    /**
     * Auxiliary data related to the block entity, such as the name of the mob
     * being spawned by a mob spawner.
     */
    @Nullable
    public String data;

    public PastryCaptureBlockEntityEvent(BlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getPos();
        this.pos = new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        if (blockEntity instanceof MobSpawnerBlockEntity mobSpawner) {
            this.name = "mob_spawner".intern();
            this.data = EntityType.getId(mobSpawner.getLogic().getRenderedEntity().getType()).getPath();
        } else {
            this.name = BlockEntityType.getId(blockEntity.getType()).getPath();
        }
    }

    public PastryCaptureBlockEntityEvent(PastryCaptureInputStream input) throws IOException {
        int x = input.readInt();
        int y = input.readInt();
        int z = input.readInt();
        this.pos = new Vec3i(x, y, z);

        this.name = input.readString();
        this.data = input.readString();
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeInt(this.pos.getX());
        output.writeInt(this.pos.getY());
        output.writeInt(this.pos.getZ());

        output.writeString(this.name);
        output.writeString(this.data);
    }
}
