package com.tesselslate.pastry.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tesselslate.pastry.gui.PastryMenuButton;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

@Mixin(value = TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text text) {
        super(text);
    }

    @WrapMethod(method = "init()V")
    private void init_addPastryButton(Operation<Void> orig) {
        orig.call();

        this.addButton(new PastryMenuButton(this, this.width / 2 - 124, this.height / 4 + 72));
    }
}
