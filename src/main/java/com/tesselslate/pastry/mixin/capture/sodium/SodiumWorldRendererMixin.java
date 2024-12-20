package com.tesselslate.pastry.mixin.capture.sodium;

import com.tesselslate.pastry.capture.PastryCaptureManager;
import com.tesselslate.pastry.capture.events.PastryCaptureBlockEntityEvent;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Set;
import java.util.SortedSet;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class SodiumWorldRendererMixin {
    @Shadow
    private ChunkRenderManager<?> chunkRenderManager;

    @Shadow
    @Final
    private Set<BlockEntity> globalBlockEntities;

    @Inject(
            at = @At("HEAD"),
            method =
                    "renderTileEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/BufferBuilderStorage;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;Lnet/minecraft/client/render/Camera;F)V",
            remap = true)
    private void renderTileEntities_recordBlockEntities(
            MatrixStack matrices,
            BufferBuilderStorage bufferBuilders,
            Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions,
            Camera camera,
            float tickDelta,
            CallbackInfo ci) {
        PastryCaptureManager.update(capture -> {
            for (BlockEntity blockEntity : this.chunkRenderManager.getVisibleBlockEntities()) {
                capture.queue(new PastryCaptureBlockEntityEvent(blockEntity));
            }
            for (BlockEntity blockEntity : this.globalBlockEntities) {
                capture.queue(new PastryCaptureBlockEntityEvent(blockEntity));
            }
        });
    }
}
