package com.tesselslate.pastry.mixin.capture;

import java.util.Objects;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.capture.PastryCaptureEvent;
import com.tesselslate.pastry.capture.PastryCaptureManager;
import com.tesselslate.pastry.capture.events.PastryCaptureFrameEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureOptionsEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureWorldLoadEvent;
import com.tesselslate.pastry.mixin.accessor.WorldRendererAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.profiler.ProfileResult;

@Mixin(value = MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(at = @At("HEAD"), method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V")
    private void joinWorld_startCapture(ClientWorld world, CallbackInfo info) {
        if (PastryCaptureManager.isCapturing()) {
            return;
        }

        PastryCaptureManager.startCapture();

        IntegratedServer server = MinecraftClient.getInstance().getServer();
        if (server != null) {
            PastryCaptureManager
                    .update(capture -> capture.add(new PastryCaptureWorldLoadEvent(server.getSaveProperties())));
        }
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;server:Lnet/minecraft/server/integrated/IntegratedServer;", opcode = Opcodes.GETFIELD), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    private IntegratedServer disconnect_stopCapture(MinecraftClient client, Operation<IntegratedServer> orig) {
        try {
            if (PastryCaptureManager.isCapturing()) {
                PastryCaptureManager.stopCapture();
            }
        } catch (Exception e) {
            Pastry.LOGGER.error("Failed to stop capture: " + e);
        }

        return orig.call(client);
    }

    @Inject(at = @At("HEAD"), method = "drawProfilerResults(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/profiler/ProfileResult;)V")
    private void drawProfilerResults_addFrameEvent(MatrixStack stack, ProfileResult profileResult, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isPaused()) {
            return;
        }

        WorldRenderer worldRenderer = client.worldRenderer;
        if (worldRenderer == null) {
            return;
        }

        Camera camera = Objects.requireNonNull(client.gameRenderer).getCamera();

        PastryCaptureManager.update(capture -> {
            // Add all queued events (entities, blockentities) from this frame to the
            // capture.
            capture.addQueued();

            PastryCaptureEvent event = new PastryCaptureFrameEvent(
                    ((WorldRendererAccessor) worldRenderer).getFrame(), camera.getPos(), camera.getPitch(),
                    camera.getYaw(), profileResult);
            capture.add(event);

            event = new PastryCaptureOptionsEvent(client);
            capture.add(event);
        });
    }
}
