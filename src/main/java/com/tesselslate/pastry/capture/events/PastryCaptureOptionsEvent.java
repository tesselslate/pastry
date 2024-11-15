package com.tesselslate.pastry.capture.events;

import java.io.IOException;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureEventType;
import com.tesselslate.pastry.capture.PastryCaptureInputStream;
import com.tesselslate.pastry.capture.PastryCaptureOutputStream;
import com.tesselslate.pastry.mixin.accessor.DebugRendererAccessor;

import net.minecraft.client.MinecraftClient;

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

    public int renderDistance;
    public int entityDistance;
    public int fov;

    public int gameWidth, gameHeight;

    public boolean hitboxes;
    public boolean chunkBorders;
    public boolean cullState;

    public PastryCaptureOptionsEvent(MinecraftClient client) {
        this.renderDistance = client.options.viewDistance;
        this.entityDistance = (int) (client.options.entityDistanceScaling * 100.0);
        this.fov = (int) client.options.fov;

        this.gameWidth = client.getWindow().getFramebufferWidth();
        this.gameHeight = client.getWindow().getFramebufferHeight();

        this.hitboxes = client.getEntityRenderManager().shouldRenderHitboxes();
        this.chunkBorders = ((DebugRendererAccessor) client.debugRenderer).getShowChunkBorder()
                && !client.hasReducedDebugInfo();
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
}
