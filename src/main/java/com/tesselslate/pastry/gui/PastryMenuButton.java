package com.tesselslate.pastry.gui;

import com.tesselslate.pastry.gui.screen.PrepareCaptureListScreen;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.StringRenderable;

public class PastryMenuButton extends ItemButton {
    public PastryMenuButton(Screen screen, int x, int y) {
        super(screen, x, y, Blocks.END_PORTAL_FRAME.asItem(), button -> {
            MinecraftClient.getInstance().openScreen(new PrepareCaptureListScreen(screen));
        }, (button, matrices, mouseX, mouseY) -> {
            screen.renderTooltip(matrices, StringRenderable.plain("Pastry"), mouseX, mouseY);
        });
    }
}
