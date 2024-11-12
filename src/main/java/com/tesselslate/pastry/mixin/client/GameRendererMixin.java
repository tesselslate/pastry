package com.tesselslate.pastry.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.capture.PastryCapture;

import net.minecraft.client.render.GameRenderer;

@Mixin(value = GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At("HEAD"), method = "render(FJZ)V")
    private void render_clearQueuedEventsAtFrameStart(float tickDelta, long startTime, boolean tick,
            CallbackInfo info) {
        PastryCapture capture = Pastry.getActiveCapture();

        if (capture != null) {
            capture.clearQueue();
        }
    }
}
