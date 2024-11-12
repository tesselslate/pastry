package com.tesselslate.pastry.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tesselslate.pastry.Pastry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.profiler.ProfileResult;

@Mixin(value = MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("HEAD"), method = "drawProfilerResults(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/profiler/ProfileResult;)V")
    private void drawProfilerResults_recordResults(MatrixStack stack, ProfileResult profileResult, CallbackInfo info) {
        Pastry.getRecorder().recordResult(profileResult);
    }

    @Inject(at = @At("HEAD"), method = "scheduleStop()V")
    private void scheduleStop_closeWriter(CallbackInfo info) {
        try {
            Pastry.getRecorder().close();
        } catch (Exception e) {
            Pastry.LOGGER.error("Failed to close writer: " + e);
        }
    }
}
