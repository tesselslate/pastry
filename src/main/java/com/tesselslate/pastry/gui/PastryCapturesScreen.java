package com.tesselslate.pastry.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class PastryCapturesScreen extends Screen {
    private Screen parent;

    public PastryCapturesScreen(Screen parent) {
        super(new LiteralText("Pastry"));

        this.parent = parent;
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        super.render(matrices, mouseX, mouseY, delta);
    }
}
