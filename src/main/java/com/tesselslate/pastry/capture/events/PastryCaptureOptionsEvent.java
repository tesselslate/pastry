package com.tesselslate.pastry.capture.events;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;
import com.tesselslate.pastry.mixin.accessor.DebugRendererAccessor;

import net.minecraft.client.MinecraftClient;

import java.io.IOException;

/**
 * Contains information about various graphical settings, such as render and
 * entity distance.
 *
 * @since format V5
 */
public class PastryCaptureOptionsEvent implements PastryCaptureEvent {
    private static final PastryCaptureEventType EVENT_TYPE = PastryCaptureEventType.OPTIONS;

    private static final int MASK_HITBOXES = 0x01000000;
    private static final int MASK_CHUNK_BORDERS = 0x02000000;
    private static final int MASK_CULL_STATE = 0x04000000;

    public final int renderDistance;
    public final int entityDistance;
    public final int fov;

    public final int gameWidth, gameHeight;

    public final boolean hitboxes;
    public final boolean chunkBorders;
    public final boolean cullState;

    public PastryCaptureOptionsEvent(MinecraftClient client) {
        this.renderDistance = client.options.viewDistance;
        this.entityDistance = (int) (client.options.entityDistanceScaling * 100.0);
        this.fov = (int) client.options.fov;

        this.gameWidth = client.getWindow().getFramebufferWidth();
        this.gameHeight = client.getWindow().getFramebufferHeight();

        this.hitboxes = client.getEntityRenderManager().shouldRenderHitboxes();
        this.chunkBorders =
                ((DebugRendererAccessor) client.debugRenderer).getShowChunkBorder() && !client.hasReducedDebugInfo();
        this.cullState = Pastry.DISPLAY_CULLING_STATE && Pastry.CAPTURED_CULLING_STATE != null;
    }

    public PastryCaptureOptionsEvent(PastryCaptureInputStream input) throws IOException {
        int packed = input.readInt();

        this.renderDistance = packed & 0xFF;
        this.entityDistance = ((packed & 0xFF00) >> 8) * 10;
        this.fov = (packed & 0xFF0000) >> 16;

        this.gameWidth = input.readInt();
        this.gameHeight = input.readInt();

        this.hitboxes = (packed & MASK_HITBOXES) != 0;
        this.chunkBorders = (packed & MASK_CHUNK_BORDERS) != 0;
        this.cullState = (packed & MASK_CULL_STATE) != 0;
    }

    @Override
    public PastryCaptureEventType getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void write(PastryCaptureOutputStream output) throws IOException {
        int packed = 0;

        packed |= renderDistance;
        packed |= (entityDistance / 10) << 8;
        packed |= fov << 16;
        packed |= this.hitboxes ? MASK_HITBOXES : 0x0;
        packed |= this.chunkBorders ? MASK_CHUNK_BORDERS : 0x0;
        packed |= this.cullState ? MASK_CULL_STATE : 0x0;

        output.writeInt(packed);

        output.writeInt(this.gameWidth);
        output.writeInt(this.gameHeight);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.renderDistance;
        result = prime * result + this.entityDistance;
        result = prime * result + this.fov;
        result = prime * result + this.gameWidth;
        result = prime * result + this.gameHeight;
        result = prime * result + (this.hitboxes ? 1231 : 1237);
        result = prime * result + (this.chunkBorders ? 1231 : 1237);
        result = prime * result + (this.cullState ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PastryCaptureOptionsEvent)) {
            return false;
        } else {
            PastryCaptureOptionsEvent other = (PastryCaptureOptionsEvent) obj;

            return (this.renderDistance == other.renderDistance)
                    && (this.entityDistance == other.entityDistance)
                    && (this.fov == other.fov)
                    && (this.gameWidth == other.gameWidth)
                    && (this.gameHeight == other.gameHeight)
                    && (this.hitboxes == other.hitboxes)
                    && (this.chunkBorders == other.chunkBorders)
                    && (this.cullState == other.cullState);
        }
    }
}
