package com.tesselslate.pastry.mixin.cullvis;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.cullvis.CullStateDebugRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(value = DebugRenderer.class)
public abstract class DebugRendererMixin {
    private CullStateDebugRenderer cullStateDebugRenderer;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/MinecraftClient;)V")
    private void init_createCullStateDebugRenderer(MinecraftClient client, CallbackInfo ci) {
        this.cullStateDebugRenderer = new CullStateDebugRenderer();
    }

    @WrapMethod(
            method =
                    "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V")
    private void render_showCullStateDebugRenderer(
            MatrixStack matrices,
            VertexConsumerProvider.Immediate vertexConsumers,
            double cameraX,
            double cameraY,
            double cameraZ,
            Operation<Void> orig) {
        orig.call(matrices, vertexConsumers, cameraX, cameraY, cameraZ);

        if (Pastry.DISPLAY_CULLING_STATE && Pastry.CAPTURED_CULLING_STATE != null) {
            this.cullStateDebugRenderer.setState(Pastry.CAPTURED_CULLING_STATE);
            this.cullStateDebugRenderer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
        }
    }
}
