package com.tesselslate.pastry.gui.screen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveAnalysis;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveReading;
import com.tesselslate.pastry.capture.PastryCaptureHeader;
import com.tesselslate.pastry.gui.ScreenExtended;
import com.tesselslate.pastry.gui.widget.PieChartWidget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class CaptureAnalysisScreen extends ScreenExtended {
    private static final String LEFT_ARROW = "\u25c0";
    private static final String RIGHT_ARROW = "\u25b6";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private final PreemptiveAnalysis analysis;

    private final LiteralText subtitle;

    private PieChartWidget pieChart;
    private ButtonWidget nextPieChartButton;
    private ButtonWidget prevPieChartButton;
    private int pieChartNumber;

    public CaptureAnalysisScreen(Screen parent, PastryCaptureHeader header, PreemptiveAnalysis analysis) {
        super(parent, new LiteralText("Analysis of " + DATE_FORMAT.format(header.recordedAt)));

        this.analysis = analysis;

        int valid = analysis.valid.getReadings().size();
        int invalid = analysis.invalid.getReadings().size();
        this.subtitle = new LiteralText(String.format("%d/%d valid readings", valid, valid + invalid));
    }

    public CaptureAnalysisScreen(Screen parent, PreemptiveAnalysis analysis) {
        super(parent, new LiteralText("Analysis"));

        this.analysis = analysis;

        int valid = analysis.valid.getReadings().size();
        int invalid = analysis.invalid.getReadings().size();
        this.subtitle = new LiteralText(String.format("%d/%d valid readings", valid, valid + invalid));
    }

    @Override
    protected void init() {
        double scaleFactor = this.client.getWindow().getScaleFactor();
        int pieChartWidth = PieChartWidget.calculateWidth(scaleFactor);
        int pieChartHeight = PieChartWidget.calculateHeight(scaleFactor, 5);
        int pieChartX = (this.width / 2) - (pieChartWidth / 2);
        int pieChartY = (this.height / 2) - (pieChartHeight / 2);

        List<PreemptiveReading> validSpikes = this.analysis.valid.getReadings();
        if (validSpikes.size() > 0) {
            this.pieChart = new PieChartWidget(this.client, pieChartX, pieChartY,
                    validSpikes.get(this.pieChartNumber).frames()[0].profiler());
        }
        this.nextPieChartButton = this.addButton(new ButtonWidget(this.width / 2 + 104, this.height - 27, 20, 20,
                new LiteralText(RIGHT_ARROW), button -> {
                    if (this.pieChartNumber < validSpikes.size() - 1) {
                        this.pieChartNumber++;
                        this.pieChart = new PieChartWidget(this.client, pieChartX, pieChartY,
                                validSpikes.get(this.pieChartNumber).frames()[0].profiler());
                    }
                }));
        this.prevPieChartButton = this.addButton(new ButtonWidget(this.width / 2 - 124, this.height - 27, 20, 20,
                new LiteralText(LEFT_ARROW), button -> {
                    if (this.pieChartNumber > 0) {
                        this.pieChartNumber--;
                        this.pieChart = new PieChartWidget(this.client, pieChartX, pieChartY,
                                validSpikes.get(this.pieChartNumber).frames()[0].profiler());
                    }
                }));

        this.addButton(this.createDoneButton(this.width / 2 - 100, this.height - 27, 200, 20));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (this.pieChart != null) {
            this.pieChart.render(matrices, mouseX, mouseY, delta);
        } else {
            // TODO: draw "no valid spikes" text
        }

        this.nextPieChartButton.render(matrices, mouseX, mouseY, delta);
        this.prevPieChartButton.render(matrices, mouseX, mouseY, delta);

        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5,
                Formatting.WHITE.getColorValue());
        this.drawCenteredText(matrices, this.textRenderer, this.subtitle, this.width / 2, 16,
                Formatting.GRAY.getColorValue());

        super.render(matrices, mouseX, mouseY, delta);
    }
}
