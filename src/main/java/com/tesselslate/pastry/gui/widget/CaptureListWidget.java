package com.tesselslate.pastry.gui.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.tesselslate.pastry.task.CaptureListTask;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class CaptureListWidget extends ElementListWidget<CaptureListWidget.Element> {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public CaptureListWidget(List<CaptureListTask.Entry> entries, MinecraftClient client, int width, int height,
            int top,
            int bottom) {
        super(client, width, height, top, bottom, client.textRenderer.fontHeight);

        for (CaptureListTask.Entry entry : entries) {
            super.addEntry(new Element(client.textRenderer, entry));
        }
    }

    public class Element extends ElementListWidget.Entry<Element> {
        private final String name;
        private final long fileSize;

        private TextRenderer textRenderer;

        public Element(TextRenderer textRenderer, CaptureListTask.Entry entry) {
            this.name = DATE_FORMAT.format(entry.header.recordedAt);
            this.fileSize = entry.size;

            this.textRenderer = textRenderer;
        }

        @Override
        public List<? extends Element> children() {
            return null;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                int mouseY, boolean hovered, float tickDelta) {
            String rightText = FileUtils.byteCountToDisplaySize(this.fileSize);
            int rightX = x + entryWidth - this.textRenderer.getWidth(rightText);

            this.textRenderer.drawWithShadow(matrices, this.name, x, y, Formatting.WHITE.getColorValue());
            this.textRenderer.drawWithShadow(matrices, rightText, rightX, y, Formatting.WHITE.getColorValue());
        }
    }
}
