package com.tesselslate.pastry.mixin.cullvis;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(value = WorldRenderer.class)
public abstract class WorldRendererMixin {
    @WrapOperation(
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/client/render/WorldRenderer;capturedFrustum:Lnet/minecraft/client/render/Frustum;",
                            opcode = Opcodes.GETFIELD),
            method =
                    "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V")
    private Frustum render_doNotUseCapturedFrustum(WorldRenderer renderer, Operation<Frustum> orig) {
        return null;
    }
}
