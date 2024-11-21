package com.tesselslate.pastry.gui;

import com.tesselslate.pastry.mixin.accessor.ScreenAccessor;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;

public abstract class ItemButton extends ButtonWidget {
    protected final Screen screen;

    protected final Item item;

    public ItemButton(Screen screen, int x, int y, Item item, PressAction action, TooltipSupplier supplier) {
        super(x, y, 20, 20, LiteralText.EMPTY, action, supplier);

        this.screen = screen;
        this.item = item;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);

        ItemRenderer itemRenderer = ((ScreenAccessor) this.screen).getItemRenderer();
        itemRenderer.renderInGui(new ItemStack(this.item), this.x + 2, this.y + 1);
    }
}
