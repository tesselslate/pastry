package com.tesselslate.pastry.mixin.debug;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tesselslate.pastry.Pastry;

import net.minecraft.client.gui.hud.DebugHud;

@Mixin(value = DebugHud.class)
public abstract class DebugHudMixin {
    @ModifyReturnValue(method = "getRightText()Ljava/util/List;", at = @At("RETURN"))
    private List<String> getRightText_addPastryVersion(List<String> orig) {
        orig.addAll(Pastry.getDebugText());

        return orig;
    }
}
