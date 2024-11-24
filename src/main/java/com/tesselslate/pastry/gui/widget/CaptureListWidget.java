package com.tesselslate.pastry.gui.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.lwjgl.glfw.GLFW;

import com.tesselslate.pastry.gui.screen.PrepareCaptureAnalysisScreen;
import com.tesselslate.pastry.task.ListCapturesTask;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class CaptureListWidget extends PaginatedListWidget<CaptureListWidget.Entry> {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public CaptureListWidget(List<ListCapturesTask.Entry> entries, Screen screen, MinecraftClient client, int width,
            int height,
            int top, int bottom, int page) {
        super(screen, client, width, height, top, bottom, client.textRenderer.fontHeight + 2, 50,
                createElements(screen, client, entries), page);
    }

    private static List<Entry> createElements(Screen screen, MinecraftClient client,
            List<ListCapturesTask.Entry> entries) {
        return entries.stream().map(entry -> new Entry(screen, client, entry)).collect(Collectors.toList());
    }

    public static class Entry extends PaginatedListWidget.Entry<Entry> {
        private TextWidget nameWidget;
        private TextWidget sizeWidget;

        public Entry(Screen screen, MinecraftClient client, ListCapturesTask.Entry entry) {
            String name = DATE_FORMAT.format(entry.header.recordedAt);
            String size = FileUtils.byteCountToDisplaySize(entry.size);

            this.nameWidget = new TextWidget(client.textRenderer, name, (mouseX, mouseY, button) -> {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    client.openScreen(new PrepareCaptureAnalysisScreen(screen, entry.path, entry.header));
                }

                return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
            });

            this.sizeWidget = new TextWidget(client.textRenderer, size);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(this.nameWidget, this.sizeWidget);
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                int mouseY, boolean hovered, float tickDelta) {
            int rightX = x + entryWidth - this.sizeWidget.getWidth();

            this.nameWidget.x = x;
            this.nameWidget.y = y;
            this.nameWidget.render(matrices, mouseX, mouseY, tickDelta);

            this.sizeWidget.x = rightX;
            this.sizeWidget.y = y;
            this.sizeWidget.render(matrices, mouseX, mouseY, tickDelta);
        }
    }
}
