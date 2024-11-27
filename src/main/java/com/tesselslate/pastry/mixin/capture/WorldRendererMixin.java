package com.tesselslate.pastry.mixin.capture;

import com.tesselslate.pastry.capture.PastryCaptureManager;
import com.tesselslate.pastry.capture.events.PastryCaptureBlockOutlineEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureEntityEvent;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;

@Mixin(value = WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(
            at = @At("HEAD"),
            method =
                    "renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V")
    private void renderEntity_recordEntity(
            Entity entity,
            double cameraX,
            double cameraY,
            double cameraZ,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            CallbackInfo ci) {
        PastryCaptureManager.update(capture -> capture.queue(new PastryCaptureEntityEvent(entity)));
    }

    @WrapMethod(
            method =
                    "drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;DDDLnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
    private void drawBlockOutline_recordBlockOutline(
            MatrixStack matrixStack,
            VertexConsumer vertexConsumer,
            Entity entity,
            double x,
            double y,
            double z,
            BlockPos blockPos,
            BlockState blockState,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> orig) {
        orig.call(matrixStack, vertexConsumer, entity, x, y, z, blockPos, blockState);

        PastryCaptureManager.update(capture -> capture.queue(new PastryCaptureBlockOutlineEvent(blockPos)));
    }
}
