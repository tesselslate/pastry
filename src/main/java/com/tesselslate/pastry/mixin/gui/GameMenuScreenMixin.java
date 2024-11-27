package com.tesselslate.pastry.mixin.gui;

import com.tesselslate.pastry.gui.PastryMenuButton;
import com.tesselslate.pastry.gui.PastryPauseButton;
import com.tesselslate.pastry.gui.PastryStartButton;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(value = GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    private GameMenuScreenMixin() {
        super(LiteralText.EMPTY);
    }

    @WrapMethod(method = "initWidgets()V")
    private void initWidgets_addPastryButtons(Operation<Void> orig) {
        orig.call();

        this.addButton(new PastryMenuButton(this, this.width / 2 - 126, this.height / 4 + 32));
        this.addButton(new PastryStartButton(this, this.width / 2 - 126, this.height / 4 + 56));
        this.addButton(new PastryPauseButton(this, this.width / 2 - 126, this.height / 4 + 80));
    }
}
