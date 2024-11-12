package com.tesselslate.pastry.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.WorldRenderer;

@Mixin(value = WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("frame")
    public int getFrame();
}
