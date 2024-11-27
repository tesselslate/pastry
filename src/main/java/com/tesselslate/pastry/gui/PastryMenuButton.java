package com.tesselslate.pastry.gui;

import com.tesselslate.pastry.gui.screen.PrepareCaptureListScreen;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;

public class PastryMenuButton extends ItemButton {
    private static final String TOOLTIP = "Pastry";

    public PastryMenuButton(Screen screen, int x, int y) {
        super(screen, x, y, Blocks.END_PORTAL_FRAME.asItem(), PastryMenuButton::onPress, PastryMenuButton::onTooltip);
    }

    private static void onPress(ButtonWidget button) {
        PastryMenuButton instance = (PastryMenuButton) button;

        MinecraftClient client = MinecraftClient.getInstance();
        client.openScreen(new PrepareCaptureListScreen(instance.screen));
    }

    private static void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        PastryMenuButton instance = (PastryMenuButton) button;

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        instance.screen.renderTooltip(
                matrices,
                StringRenderable.plain(TOOLTIP),
                instance.x - 20 - textRenderer.getWidth(TOOLTIP),
                instance.y + 22 - (textRenderer.fontHeight / 2));
    }
}
