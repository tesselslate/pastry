package com.tesselslate.pastry.mixin.accessor;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("capturedFrustum")
    public Frustum getCapturedFrustum();

    @Accessor("capturedFrustum")
    public void setCapturedFrustum(Frustum value);

    @Accessor("shouldCaptureFrustum")
    public void setShouldCaptureFrustum(boolean value);
}
