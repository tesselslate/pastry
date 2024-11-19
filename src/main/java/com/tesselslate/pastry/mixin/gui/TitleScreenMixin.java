package com.tesselslate.pastry.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tesselslate.pastry.gui.CaptureListScreen;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(value = TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text text) {
        super(text);
    }

    @WrapMethod(method = "init()V")
    private void init_addPastryButton(Operation<Void> orig) {
        orig.call();

        this.addButton(new Button(this.width / 2 - 124, this.height / 4 + 72, 20, 20, LiteralText.EMPTY, button -> {
            MinecraftClient.getInstance().openScreen(new CaptureListScreen(this));
        }));
    }

    private class Button extends ButtonWidget {
        private Button(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.renderButton(matrices, mouseX, mouseY, delta);

            itemRenderer.renderInGui(new ItemStack(Blocks.END_PORTAL_FRAME.asItem()), this.x + 2, this.y + 1);
        }
    }
}
