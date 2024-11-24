package com.tesselslate.pastry.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveReading;
import com.tesselslate.pastry.capture.events.PastryCaptureBlockEntityEvent;
import com.tesselslate.pastry.capture.events.PastryCaptureEntityEvent;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class CaptureAnalysisPageWidget extends AbstractParentElement implements Drawable {
    private final Screen screen;
    private final PreemptiveReading reading;

    private PieChartWidget pieChart;
    private TextWidget entitiesText;
    private TextWidget blockEntitiesText;
    private TextWidget blockOutlineText;

    private List<StringRenderable> entitiesTooltip;
    private List<StringRenderable> blockEntitiesTooltip;

    public CaptureAnalysisPageWidget(Screen screen, PreemptiveReading reading) {
        this.screen = screen;
        this.reading = reading;

        MinecraftClient client = MinecraftClient.getInstance();
        double scaleFactor = client.getWindow().getScaleFactor();

        int pieChartWidth = PieChartWidget.calculateWidth(scaleFactor);
        int pieChartHeight = PieChartWidget.calculateHeight(scaleFactor, 5);
        int pieChartX = (screen.width / 2) - (pieChartWidth / 2);
        int pieChartY = (screen.height / 2) - (pieChartHeight / 2);

        this.pieChart = new PieChartWidget(client, pieChartX, pieChartY, reading.frames()[0].profiler());

        int textY = pieChartY + (pieChartHeight / 2) - ((client.textRenderer.fontHeight * 3 + 4) / 2);
        this.initializeTextWidgets(client.textRenderer, pieChartX - 80, textY);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.pieChart, this.entitiesText, this.blockEntitiesText, this.blockOutlineText);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.pieChart.render(matrices, mouseX, mouseY, delta);

        this.entitiesText.render(matrices, mouseX, mouseY, delta);
        this.blockEntitiesText.render(matrices, mouseX, mouseY, delta);
        this.blockOutlineText.render(matrices, mouseX, mouseY, delta);
    }

    private void onEntityTooltip(TextWidget text, MatrixStack matrices, int mouseX, int mouseY) {
        this.screen.renderTooltip(matrices, this.entitiesTooltip, mouseX, mouseY);
    }

    private void onBlockEntityTooltip(TextWidget text, MatrixStack matrices, int mouseX, int mouseY) {
        this.screen.renderTooltip(matrices, this.blockEntitiesTooltip, mouseX, mouseY);
    }

    private void initializeTextWidgets(TextRenderer textRenderer, int startX, int startY) {
        initializeEntityText(textRenderer, startX, startY);
        initializeBlockEntityText(textRenderer, startX, startY);
        initializeBlockOutlineText(textRenderer, startX, startY);
    }

    private void initializeEntityText(TextRenderer textRenderer, int startX, int startY) {
        PastryCaptureEntityEvent[] entities = this.reading.frames()[0].entities();

        Object2IntArrayMap<String> entityCounts = new Object2IntArrayMap<>();
        for (PastryCaptureEntityEvent entity : entities) {
            int count = entityCounts.getInt(entity.name);
            entityCounts.put(entity.name, count + 1);
        }

        int totalEntities = entityCounts.values().stream().collect(Collectors.summingInt(Integer::intValue));
        String text = String.format("Entities: %d", totalEntities);

        this.entitiesText = new TextWidget(textRenderer, text, null, this::onEntityTooltip);
        this.entitiesText.x = startX + 40 - textRenderer.getWidth(text);
        this.entitiesText.y = startY;

        this.entitiesTooltip = new ArrayList<>();
        for (Object2IntMap.Entry<String> entry : entityCounts.object2IntEntrySet()) {
            String line = String.format("%s: %d", entry.getKey(), entry.getIntValue());
            this.entitiesTooltip.add(StringRenderable.plain(line));
        }
    }

    private void initializeBlockEntityText(TextRenderer textRenderer, int startX, int startY) {
        PastryCaptureBlockEntityEvent[] blockEntities = this.reading.frames()[0].blockEntities();

        Object2IntArrayMap<String> blockEntityCounts = new Object2IntArrayMap<>();
        for (PastryCaptureBlockEntityEvent blockEntity : blockEntities) {
            String key;

            if (blockEntity.name.equals("mob_spawner")) {
                key = String.format("mob_spawner (%s)", blockEntity.data);
            } else {
                key = blockEntity.name;
            }

            int count = blockEntityCounts.getInt(key);
            blockEntityCounts.put(key, count + 1);
        }

        int totalBlockEntities = blockEntityCounts.values().stream().collect(Collectors.summingInt(Integer::intValue));
        String text = String.format("Block Entities: %d", totalBlockEntities);

        this.blockEntitiesText = new TextWidget(textRenderer, text, null,
                this::onBlockEntityTooltip);
        this.blockEntitiesText.x = startX + 40 - textRenderer.getWidth(text);
        this.blockEntitiesText.y = startY + textRenderer.fontHeight + 2;

        this.blockEntitiesTooltip = new ArrayList<>();
        for (Object2IntMap.Entry<String> entry : blockEntityCounts.object2IntEntrySet()) {
            String line = String.format("%s: %d", entry.getKey(), entry.getIntValue());
            this.blockEntitiesTooltip.add(StringRenderable.plain(line));
        }
    }

    private void initializeBlockOutlineText(TextRenderer textRenderer, int startX, int startY) {
        boolean blockOutline = this.reading.frames()[0].blockOutline() != null;

        Style style = Style.EMPTY.withFormatting(blockOutline ? Formatting.GREEN : Formatting.RED);
        StringRenderable text = StringRenderable.concat(StringRenderable.plain("Block Outline: "),
                StringRenderable.styled(blockOutline ? "Yes" : "No", style));

        this.blockOutlineText = new TextWidget(textRenderer, text, null, null);
        this.blockOutlineText.x = startX + 40 - textRenderer.getWidth(text);
        this.blockOutlineText.y = startY + (textRenderer.fontHeight * 2) + 4;
    }
}
