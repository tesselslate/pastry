package com.tesselslate.pastry.gui;

import com.tesselslate.pastry.mixin.accessor.ScreenAccessor;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;

public class PastryIconButton extends ButtonWidget {
    private final Screen screen;

    public PastryIconButton(Screen screen, int x, int y, PressAction action) {
        super(x, y, 20, 20, LiteralText.EMPTY, action, (button, matrices, mouseX, mouseY) -> {
            screen.renderTooltip(matrices, StringRenderable.plain("Pastry"), mouseX, mouseY);
        });

        this.screen = screen;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);

        ItemRenderer itemRenderer = ((ScreenAccessor) this.screen).getItemRenderer();
        itemRenderer.renderInGui(new ItemStack(Blocks.END_PORTAL_FRAME.asItem()), this.x + 2, this.y + 1);
    }
}
