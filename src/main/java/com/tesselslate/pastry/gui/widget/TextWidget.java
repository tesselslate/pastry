package com.tesselslate.pastry.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextWidget implements Drawable, Element {
    private final TextRenderer textRenderer;
    private final String text;
    private final Callback onClick;

    public int x;
    public int y;

    private int cachedWidth = Integer.MIN_VALUE;

    public TextWidget(TextRenderer textRenderer, String text) {
        this(textRenderer, text, null);
    }

    public TextWidget(TextRenderer textRenderer, String text, Callback onClick) {
        this.textRenderer = textRenderer;
        this.text = text;
        this.onClick = onClick;
    }

    public int getWidth() {
        if (this.cachedWidth != Integer.MIN_VALUE) {
            return this.cachedWidth;
        }

        this.cachedWidth = this.textRenderer.getWidth(this.text);
        return this.cachedWidth;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        boolean shouldUnderline = this.onClick != null && this.isMouseOver(mouseX, mouseY);
        Text text = new LiteralText(this.text).formatted(shouldUnderline ? Formatting.UNDERLINE : Formatting.RESET);

        this.textRenderer.draw(matrices, text, this.x, this.y, Formatting.WHITE.getColorValue());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.onClick == null || !this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        return this.onClick.run(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.getWidth() && mouseY >= this.y
                && mouseY < this.y + this.textRenderer.fontHeight;
    }

    public interface Callback {
        public boolean run(double mouseX, double mouseY, int button);
    }
}
