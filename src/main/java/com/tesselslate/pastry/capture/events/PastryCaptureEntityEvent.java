package com.tesselslate.pastry.capture.events;

import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;

/**
 * Contains information about a single visible entity.
 *
 * @since format V1
 */
public class PastryCaptureEntityEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.ENTITY;

    /**
     * The world coordinates of the entity.
     */
    public Vec3d pos;

    /**
     * The ID of the entity (namespace not included.)
     */
    @NotNull
    public String name;

    public PastryCaptureEntityEvent(Entity entity) {
        this.pos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        this.name = EntityType.getId(entity.getType()).getPath();
        Objects.requireNonNull(this.name);
    }

    public PastryCaptureEntityEvent(PastryCaptureInputStream input) throws IOException {
        double entityX = input.readDouble();
        double entityY = input.readDouble();
        double entityZ = input.readDouble();

        this.pos = new Vec3d(entityX, entityY, entityZ);
        this.name = input.readString();
        Objects.requireNonNull(this.name);
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        output.writeDouble(this.pos.x);
        output.writeDouble(this.pos.y);
        output.writeDouble(this.pos.z);

        output.writeString(this.name);
    }
}
