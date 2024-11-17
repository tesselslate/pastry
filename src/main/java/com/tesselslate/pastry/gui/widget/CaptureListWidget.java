package com.tesselslate.pastry.gui.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.tesselslate.pastry.task.ListCapturesTask;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class CaptureListWidget extends PaginatedListWidget<CaptureListWidget.Element> {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public CaptureListWidget(List<ListCapturesTask.Entry> entries, Screen screen, MinecraftClient client, int width,
            int height,
            int top, int bottom, int page) {
        super(screen, client, width, height, top, bottom, client.textRenderer.fontHeight, 30,
                createElements(client, entries),
                page);
    }

    private static List<Element> createElements(MinecraftClient client, List<ListCapturesTask.Entry> entries) {
        return entries.stream().map(entry -> new Element(client.textRenderer, entry)).collect(Collectors.toList());
    }

    public static class Element extends PaginatedListWidget.Entry<Element> {
        private final String name;
        private final long fileSize;

        private TextRenderer textRenderer;

        public Element(TextRenderer textRenderer, ListCapturesTask.Entry entry) {
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
