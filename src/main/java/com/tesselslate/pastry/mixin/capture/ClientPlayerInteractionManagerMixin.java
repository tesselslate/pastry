package com.tesselslate.pastry.mixin.capture;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tesselslate.pastry.capture.PastryCaptureManager;
import com.tesselslate.pastry.capture.events.PastryCaptureGamemodeEvent;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.world.GameMode;

@Mixin(value = ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @WrapMethod(method = "setGameMode(Lnet/minecraft/world/GameMode;)V")
    private void setGameMode_addGameModeEvent(GameMode gameMode,
            Operation<Void> orig) {
        orig.call(gameMode);

        PastryCaptureManager.update(capture -> capture.add(new PastryCaptureGamemodeEvent(gameMode)));
    }
}
