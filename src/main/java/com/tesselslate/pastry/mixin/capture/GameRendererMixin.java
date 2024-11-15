package com.tesselslate.pastry.mixin.capture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tesselslate.pastry.capture.PastryCaptureManager;

import net.minecraft.client.render.GameRenderer;

@Mixin(value = GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(at = @At("HEAD"), method = "render(FJZ)V")
    private void render_clearQueuedEventsAtFrameStart(float tickDelta, long startTime, boolean tick,
            CallbackInfo info) {
        PastryCaptureManager.update(capture -> capture.clearQueue());
    }
}
