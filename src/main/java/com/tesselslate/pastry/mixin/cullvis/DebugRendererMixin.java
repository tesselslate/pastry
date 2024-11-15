package com.tesselslate.pastry.mixin.cullvis;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.cullvis.CullingVisualizer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(value = DebugRenderer.class)
public abstract class DebugRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void render_displayCullingVisualization(MatrixStack matrices,
            VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ,
            CallbackInfo info) {
        if (!Pastry.DISPLAY_CULLING_STATE || Pastry.CAPTURED_CULLING_STATE == null) {
            return;
        }

        @SuppressWarnings("resource")
        CullingVisualizer visualizer = new CullingVisualizer(Pastry.CAPTURED_CULLING_STATE,
                MinecraftClient.getInstance().gameRenderer.getCamera());

        visualizer.render();
    }
}
