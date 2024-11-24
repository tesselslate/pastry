package com.tesselslate.pastry.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public abstract class IconButton extends ButtonWidget {
    protected final Screen screen;

    protected Identifier texture;

    public IconButton(Screen screen, int x, int y, Identifier texture, PressAction action, TooltipSupplier supplier) {
        super(x, y, 20, 20, LiteralText.EMPTY, action, supplier);

        this.screen = screen;
        this.texture = texture;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);

        MinecraftClient client = MinecraftClient.getInstance();
        client.getTextureManager().bindTexture(this.texture);

        drawTexture(matrices, this.x, this.y, 0.0f, 0.0f, 20, 20, 20, 20);
    }
}
