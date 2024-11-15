package com.tesselslate.pastry.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.debug.DebugRenderer;

@Mixin(value = DebugRenderer.class)
public interface DebugRendererAccessor {
    @Accessor("showChunkBorder")
    public boolean getShowChunkBorder();
}
