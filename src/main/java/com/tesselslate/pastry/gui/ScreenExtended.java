package com.tesselslate.pastry.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public abstract class ScreenExtended extends Screen {
    protected final Screen parent;

    protected ScreenExtended(Screen parent, Text title) {
        super(title);

        this.parent = parent;
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);

        if (this.parent == null || this.parent instanceof TitleScreen) {
            this.client.getToastManager().clear();
        }
    }

    protected ButtonWidget createDoneButton(int x, int y, int width, int height) {
        return new ButtonWidget(x, y, width, height, ScreenTexts.DONE, button -> {
            this.client.openScreen(this.parent);

            if (this.parent == null || this.parent instanceof TitleScreen) {
                this.client.getToastManager().clear();
            }
        });
    }
}
