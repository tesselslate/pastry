package com.tesselslate.pastry.gui.screen;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.tesselslate.pastry.gui.ScreenExtended;
import com.tesselslate.pastry.gui.widget.CaptureListWidget;
import com.tesselslate.pastry.task.ListCapturesTask;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class CaptureListScreen extends ScreenExtended {
    private final List<ListCapturesTask.Entry> captures;
    private final LiteralText subtitle;

    private CaptureListWidget captureList;

    public CaptureListScreen(Screen parent, ListCapturesTask.Result result) {
        super(parent, new LiteralText("Pastry Captures"));

        this.captures = result.entries;
        int entryCount = this.captures.size();

        String totalSize = FileUtils.byteCountToDisplaySize(
                this.captures.stream().map(entry -> entry.size).collect(Collectors.summingLong(Long::longValue)));
        this.subtitle = new LiteralText(
                String.format("%d %s (%s)", entryCount, entryCount != 1 ? "captures" : "capture", totalSize));
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    protected void init() {
        this.captureList = this.addChild(new CaptureListWidget(this.captures, this, this.client, this.width,
                this.height, 32, this.height - 32, this.captureList != null ? this.captureList.getPage() : 0));
        this.addButton(this.captureList.createNextPageButton(this.width / 2 + 104, this.height - 27, 20, 20));
        this.addButton(this.captureList.createPrevPageButton(this.width / 2 - 124, this.height - 27, 20, 20));

        this.addButton(new ButtonWidget(this.width / 2 + 2, this.height - 27, 98, 20, new LiteralText("Analyze All"),
                button -> this.client.openScreen(new PrepareMultiCaptureAnalysisScreen(this, this.captures))));
        this.addButton(this.createDoneButton(this.width / 2 - 100, this.height - 27, 98, 20));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        this.captureList.render(matrices, mouseX, mouseY, delta);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5,
                Formatting.WHITE.getColorValue());
        this.drawCenteredText(matrices, this.textRenderer, this.subtitle, this.width / 2, 16,
                Formatting.GRAY.getColorValue());

        super.render(matrices, mouseX, mouseY, delta);
    }
}
