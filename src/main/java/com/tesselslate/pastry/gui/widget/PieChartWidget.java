package com.tesselslate.pastry.gui.widget;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveReadingAverage;
import com.tesselslate.pastry.capture.events.PastryCaptureProfilerEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.ProfilerTiming;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Draws the debug profiler pie chart as it appears ingame, including the lack
 * of scaling.
 *
 * @see MinecraftClient#drawProfilerResults()
 */
public class PieChartWidget implements Drawable, Element {
    /**
     * The width of the pie chart in screen pixels.
     *
     * @see #calculateWidth(double)
     */
    private static final int WIDTH = 320;

    /**
     * The height of the pie chart in screen pixels.
     *
     * @see #calculateHeight(double, int)
     */
    private static final int HEIGHT = 170;

    private final int x;
    private final int y;

    private final MinecraftClient client;

    private final List<PieChartWidget.Entry> profileResults;

    public PieChartWidget(MinecraftClient client, int x, int y, PastryCaptureProfilerEvent profilerEvent) {
        this(client, x, y, createEntryList(profilerEvent));
    }

    public PieChartWidget(MinecraftClient client, int x, int y, PreemptiveReadingAverage average) {
        this(client, x, y, createEntryList(average));
    }

    private PieChartWidget(MinecraftClient client, int x, int y, List<PieChartWidget.Entry> profileResults) {
        this.x = x;
        this.y = y;

        this.client = client;

        this.profileResults = new ArrayList<>(profileResults);
        this.profileResults.sort((a, b) -> Double.compare(b.parentPercentage, a.parentPercentage));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
        RenderSystem.disableTexture();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        this.disableScaleFactor();

        this.renderPieSlices(bufferBuilder);
        this.renderPieSides(bufferBuilder);
        RenderSystem.enableTexture();
        this.renderPieText(matrices);

        this.enableScaleFactor();
    }

    /**
     * Calculates the width in UI pixels of a {@link PieChartWidget} rendered with
     * the given {@code scaleFactor}.
     *
     * @return The width in UI pixels of a {@link PieChartWidget} with the given
     *         {@code scaleFactor}.
     */
    public static int calculateWidth(double scaleFactor) {
        return (int) (WIDTH / scaleFactor);
    }

    /**
     * Calculates the height in UI pixels of a {@link PieChartWidget} rendered with
     * the given {@code scaleFactor} and number of profiler results.
     *
     * @return The height in UI pixels of a {@link PieChartWidget} with the given
     *         configuration
     */
    public static int calculateHeight(double scaleFactor, int resultsCount) {
        return (int) ((HEIGHT + 20 + resultsCount * 8) / scaleFactor);
    }

    @SuppressWarnings("deprecation")
    private void disableScaleFactor() {
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(
                0.0,
                this.client.getWindow().getFramebufferWidth(),
                this.client.getWindow().getFramebufferHeight(),
                0.0,
                1000.0,
                3000.0);

        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0f, 0.0f, -2000.0f);
    }

    @SuppressWarnings("deprecation")
    private void enableScaleFactor() {
        double scaleFactor = this.client.getWindow().getScaleFactor();

        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(
                0.0,
                (double) this.client.getWindow().getFramebufferWidth() / scaleFactor,
                (double) this.client.getWindow().getFramebufferHeight() / scaleFactor,
                0.0,
                1000.0,
                3000.0);

        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0f, 0.0f, -2000.0f);
    }

    private void renderPieSlices(BufferBuilder bufferBuilder) {
        int centerX = (int) (this.x * this.client.getWindow().getScaleFactor()) + (WIDTH / 2);
        int centerY = (int) (this.y * this.client.getWindow().getScaleFactor()) + 80;

        double percent = 0;
        for (PieChartWidget.Entry entry : this.profileResults) {
            int color = entry.getColor();
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            int iterations = MathHelper.floor(entry.parentPercentage / 4.0) + 1;

            bufferBuilder.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(centerX, centerY, 0.0).color(r, g, b, 0xFF).next();
            for (int i = iterations; i >= 0; i--) {
                float iterationAngle = (float)
                        ((percent + entry.parentPercentage * (double) i / (double) iterations) * Math.TAU / 100.0);

                float x = MathHelper.sin(iterationAngle) * 160.0f;
                float y = MathHelper.cos(iterationAngle) * 160.0f * 0.5f;

                bufferBuilder
                        .vertex(centerX + x, centerY - y, 0.0)
                        .color(r, g, b, 0xFF)
                        .next();
            }
            Tessellator.getInstance().draw();

            percent += entry.parentPercentage;
        }
    }

    private void renderPieSides(BufferBuilder bufferBuilder) {
        int centerX = (int) (this.x * this.client.getWindow().getScaleFactor()) + (WIDTH / 2);
        int centerY = (int) (this.y * this.client.getWindow().getScaleFactor()) + 80;

        double percent = 0;
        for (PieChartWidget.Entry entry : this.profileResults) {
            int color = entry.getColor();
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            int iterations = MathHelper.floor(entry.parentPercentage / 4.0) + 1;

            bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = iterations; i >= 0; i--) {
                float iterationAngle = (float)
                        ((percent + entry.parentPercentage * (double) i / (double) iterations) * Math.TAU / 100.0);

                float x = MathHelper.sin(iterationAngle) * 160.0f;
                float y = MathHelper.cos(iterationAngle) * 160.0f * 0.5f;

                if (y > 0.0f) {
                    continue;
                }

                bufferBuilder
                        .vertex(centerX + x, centerY - y, 0.0)
                        .color(r >> 1, g >> 1, b >> 1, 0xFF)
                        .next();
                bufferBuilder
                        .vertex(centerX + x, centerY - y + 10.0f, 0.0)
                        .color(r >> 1, g >> 1, b >> 1, 0xFF)
                        .next();
            }
            Tessellator.getInstance().draw();

            percent += entry.parentPercentage;
        }
    }

    private void renderPieText(MatrixStack matrices) {
        int x = (int) (this.x * this.client.getWindow().getScaleFactor());
        int y = (int) (this.y * this.client.getWindow().getScaleFactor());
        int centerX = x + (WIDTH / 2);

        DecimalFormat decimalFormat = new DecimalFormat("##0.00");
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));

        for (int i = 0; i < this.profileResults.size(); i++) {
            PieChartWidget.Entry entry = this.profileResults.get(i);

            String name = (entry.name.equals("unspecified") ? "[?] " : ("[" + (i + 1) + "] ")) + entry.name;
            String parent = decimalFormat.format(entry.parentPercentage) + "%";
            String total = decimalFormat.format(entry.totalPercentage) + "%";

            int parentWidth = this.client.textRenderer.getWidth(parent);
            int totalWidth = this.client.textRenderer.getWidth(total);

            int rowY = y + HEIGHT + 20 + i * 8;

            this.client.textRenderer.drawWithShadow(matrices, name, x, rowY, entry.getColor());
            this.client.textRenderer.drawWithShadow(
                    matrices, parent, centerX + 110 - parentWidth, rowY, entry.getColor());
            this.client.textRenderer.drawWithShadow(
                    matrices, total, centerX + 160 - totalWidth, rowY, entry.getColor());
        }
    }

    private static List<PieChartWidget.Entry> createEntryList(PastryCaptureProfilerEvent event) {
        ArrayList<PieChartWidget.Entry> entries = new ArrayList<>();

        entries.add(new PieChartWidget.Entry(
                "blockentities", event.blockEntityParentPercentage, event.blockEntityTotalPercentage));
        entries.add(new PieChartWidget.Entry("entities", event.entityParentPercentage, event.entityTotalPercentage));
        entries.add(new PieChartWidget.Entry(
                "unspecified", event.unspecifiedParentPercentage, event.unspecifiedTotalPercentage));
        entries.add(new PieChartWidget.Entry(
                "destroyProgress", event.destroyProgressParentPercentage, event.destroyProgressTotalPercentage));
        entries.add(new PieChartWidget.Entry("prepare", event.prepareParentPercentage, event.prepareTotalPercentage));

        return entries;
    }

    private static List<PieChartWidget.Entry> createEntryList(PreemptiveReadingAverage average) {
        ArrayList<PieChartWidget.Entry> entries = new ArrayList<>();

        entries.add(new PieChartWidget.Entry(
                "blockentities", average.blockEntityParentPercentage, average.blockEntityTotalPercentage));
        entries.add(
                new PieChartWidget.Entry("entities", average.entityParentPercentage, average.entityTotalPercentage));
        entries.add(new PieChartWidget.Entry(
                "unspecified", average.unspecifiedParentPercentage, average.unspecifiedTotalPercentage));
        entries.add(new PieChartWidget.Entry(
                "destroyProgress", average.destroyProgressParentPercentage, average.destroyProgressTotalPercentage));
        entries.add(
                new PieChartWidget.Entry("prepare", average.prepareParentPercentage, average.prepareTotalPercentage));

        return entries;
    }

    private static class Entry {
        private final String name;
        private final double parentPercentage;
        private final double totalPercentage;

        public Entry(String name, double parentPercentage, double totalPercentage) {
            this.name = name;
            this.parentPercentage = parentPercentage;
            this.totalPercentage = totalPercentage;
        }

        /**
         * @see ProfilerTiming#getColor()
         */
        private int getColor() {
            return (this.name.hashCode() & 0xAAAAAA) + 0x444444;
        }
    }
}
