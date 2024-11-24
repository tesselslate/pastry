package com.tesselslate.pastry.mixin.capture;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.capture.PastryCaptureManager;
import com.tesselslate.pastry.capture.events.PastryCaptureDimensionEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureFrameEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureOptionsEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureProfilerEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureSysinfoEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureWorldLoadEvent;
import com.tesselslate.pastry.mixin.accessor.WorldRendererAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.profiler.ProfileResult;

@Mixin(value = MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    private ProfileResult tickProfilerResult;

    @Inject(at = @At("HEAD"), method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V")
    private void joinWorld_startCapture(ClientWorld world, CallbackInfo ci) {
        boolean capturing = PastryCaptureManager.isCapturing();

        if (!capturing) {
            PastryCaptureManager.startCapture();
        }

        PastryCaptureManager.update(capture -> {
            capture.add(new PastryCaptureSysinfoEvent());

            IntegratedServer server = MinecraftClient.getInstance().getServer();
            if (!capturing && server != null) {
                capture.add(new PastryCaptureWorldLoadEvent(server.getSaveProperties()));
            }

            capture.add(new PastryCaptureDimensionEvent(world));
        });
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

    @Inject(at = @At("HEAD"), method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V")
    private void joinWorld_resetCullState(ClientWorld world, CallbackInfo ci) {
        // Reset the captured culling state and disable the debug renderer.
        Pastry.CAPTURED_CULLING_STATE = null;
        Pastry.DISPLAY_CULLING_STATE = false;

        // Reset the captured frustum (if any.)
        WorldRenderer worldRenderer = ((MinecraftClient) (Object) this).worldRenderer;
        ((WorldRendererAccessor) worldRenderer).setCapturedFrustum(null);
    }

    @Inject(at = @At("HEAD"), method = "render(Z)V")
    private void render_clearQueuedEvents(boolean tick, CallbackInfo ci) {
        PastryCaptureManager.update(capture -> capture.clearQueue());
    }

    @Inject(at = @At("TAIL"), method = "render(Z)V")
    private void render_addFrameEvents(boolean tick, CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.isPaused()) {
            return;
        }

        WorldRenderer worldRenderer = client.worldRenderer;
        if (worldRenderer == null) {
            return;
        }

        Camera camera = client.gameRenderer.getCamera();

        PastryCaptureManager.update(capture -> {
            if (this.tickProfilerResult != null) {
                // Only capture entities and block entities if the pie chart is open on this
                // frame.
                capture.addQueued();

                capture.add(new PastryCaptureProfilerEvent(this.tickProfilerResult));
            }

            capture.add(new PastryCaptureFrameEvent(PastryCaptureManager.getElapsedTime(), camera, client.world.getRegularEntityCount()));
            capture.add(new PastryCaptureOptionsEvent(client));
        });
    }
}
