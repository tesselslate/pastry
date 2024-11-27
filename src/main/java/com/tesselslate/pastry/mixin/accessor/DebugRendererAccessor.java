package com.tesselslate.pastry.mixin.accessor;

import net.minecraft.client.render.debug.DebugRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DebugRenderer.class)
public interface DebugRendererAccessor {
    @Accessor("showChunkBorder")
    public boolean getShowChunkBorder();
}
