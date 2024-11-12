package com.tesselslate.pastry.mixin.client;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.capture.PastryCapture;
import com.tesselslate.pastry.capture.events.PastryCaptureFrameEvent;
import com.tesselslate.pastry.mixin.accessor.WorldRendererAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.ProfileResult;

@Mixin(value = MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("HEAD"), method = "drawProfilerResults(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/profiler/ProfileResult;)V")
    private void drawProfilerResults_addFrameEvent(MatrixStack stack, ProfileResult profileResult, CallbackInfo info) {
        PastryCapture capture = Pastry.getActiveCapture();
        if (capture == null) {
            return;
        }

        @SuppressWarnings("resource")
        WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
        if (worldRenderer == null) {
            return;
        }

        @SuppressWarnings("resource")
        Camera camera = Objects.requireNonNull(MinecraftClient.getInstance().gameRenderer).getCamera();

        // Add all queued events (entities, blockentities) from this frame to the
        // capture.
        capture.addQueued();

        PastryCaptureFrameEvent event = new PastryCaptureFrameEvent(((WorldRendererAccessor) worldRenderer).getFrame(),
                camera.getPos(), camera.getPitch(), camera.getYaw(), profileResult);
        capture.add(event);
    }

    @Inject(at = @At("HEAD"), method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V")
    private void joinWorld_startCapture(ClientWorld world, CallbackInfo info) {
        Pastry.startCapture();
    }

    @Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    private void disconnect_stopCapture(Screen screen, CallbackInfo info) {
        if (!(screen instanceof ProgressScreen)) {
            Pastry.endCapture();
        }
    }
}
