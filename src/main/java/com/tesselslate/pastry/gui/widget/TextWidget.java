package com.tesselslate.pastry.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextWidget implements Drawable, Element {
    private final TextRenderer textRenderer;
    private final StringRenderable text;
    private final Callback onClick;
    private final TooltipSupplier tooltipSupplier;

    public int x;
    public int y;

    private int cachedWidth = Integer.MIN_VALUE;

    public TextWidget(TextRenderer textRenderer, String text) {
        this(textRenderer, text, null, null);
    }

    public TextWidget(TextRenderer textRenderer, String text, Callback onClick) {
        this(textRenderer, text, onClick, null);
    }

    public TextWidget(TextRenderer textRenderer, String text, Callback onClick, TooltipSupplier tooltipSupplier) {
        this(textRenderer, StringRenderable.plain(text), onClick, tooltipSupplier);
    }

    public TextWidget(
            TextRenderer textRenderer, StringRenderable text, Callback onClick, TooltipSupplier tooltipSupplier) {
        this.textRenderer = textRenderer;
        this.text = text;
        this.onClick = onClick;
        this.tooltipSupplier = tooltipSupplier;
    }

    public int getWidth() {
        if (this.cachedWidth != Integer.MIN_VALUE) {
            return this.cachedWidth;
        }

        this.cachedWidth = this.textRenderer.getWidth(this.text);
        return this.cachedWidth;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isMouseOver(mouseX, mouseY);
        boolean shouldUnderline = this.onClick != null && hovered;

        if (shouldUnderline) {
            // TODO: Support underlining styled text
            Text text = new LiteralText(this.text.getString()).formatted(Formatting.UNDERLINE);
            this.textRenderer.drawWithShadow(matrices, text, this.x, this.y, Formatting.WHITE.getColorValue());
        } else {
            this.textRenderer.drawWithShadow(matrices, this.text, this.x, this.y, Formatting.WHITE.getColorValue());
        }

        if (hovered && this.tooltipSupplier != null) {
            this.tooltipSupplier.onTooltip(this, matrices, mouseX, mouseY);
        }
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
        return mouseX >= this.x
                && mouseX < this.x + this.getWidth()
                && mouseY >= this.y
                && mouseY < this.y + this.textRenderer.fontHeight;
    }

    public interface Callback {
        public boolean run(double mouseX, double mouseY, int button);
    }

    public interface TooltipSupplier {
        public void onTooltip(TextWidget widget, MatrixStack matrices, int mouseX, int mouseY);
    }
}
