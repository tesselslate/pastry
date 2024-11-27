package com.tesselslate.pastry.gui.screen;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveAnalysis;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveAnalysisResult;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveReading;
import com.tesselslate.pastry.capture.PastryCaptureHeader;
import com.tesselslate.pastry.gui.ScreenExtended;
import com.tesselslate.pastry.gui.widget.CaptureAnalysisPageWidget;
import com.tesselslate.pastry.gui.widget.FrameSliderWidget;
import com.tesselslate.pastry.gui.widget.PieChartWidget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class CaptureAnalysisScreen extends ScreenExtended {
    private static final String LEFT_ARROW = "\u25c0";
    private static final String RIGHT_ARROW = "\u25b6";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private PreemptiveAnalysisResult analysisResult;

    private final LiteralText subtitle;

    private CaptureAnalysisPageWidget page;
    private FrameSliderWidget frameSlider;

    private int pageNumber;

    public CaptureAnalysisScreen(Screen parent, PastryCaptureHeader header, PreemptiveAnalysis analysis) {
        super(parent, new LiteralText("Analysis of " + DATE_FORMAT.format(header.recordedAt)));

        this.analysisResult = analysis.process();

        int valid = this.analysisResult.valid.getReadings().size();
        int invalid = this.analysisResult.invalid.getReadings().size();
        this.subtitle = new LiteralText(String.format("%d/%d valid readings", valid, valid + invalid));
    }

    public CaptureAnalysisScreen(Screen parent, PreemptiveAnalysis analysis) {
        super(parent, new LiteralText("Analysis"));

        this.analysisResult = analysis.process();

        int valid = this.analysisResult.valid.getReadings().size();
        int invalid = this.analysisResult.invalid.getReadings().size();
        this.subtitle = new LiteralText(String.format("%d/%d valid readings", valid, valid + invalid));
    }

    @Override
    protected void init() {
        List<PreemptiveReading> validSpikes = this.analysisResult.valid.getReadings();
        if (validSpikes.size() > 0) {
            this.initPageWidgets(this.page == null);
        }

        this.addButton(new ButtonWidget(
                this.width / 2 + 104, this.height - 27, 20, 20, new LiteralText(RIGHT_ARROW), button -> {
                    if (this.pageNumber < validSpikes.size() - 1) {
                        this.pageNumber++;
                        this.initPageWidgets(true);
                    }
                }));
        this.addButton(new ButtonWidget(
                this.width / 2 - 124, this.height - 27, 20, 20, new LiteralText(LEFT_ARROW), button -> {
                    if (this.pageNumber > 0) {
                        this.pageNumber--;
                        this.initPageWidgets(true);
                    }
                }));

        this.addButton(this.createDoneButton(this.width / 2 - 100, this.height - 27, 200, 20));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (this.page != null) {
            this.page.render(matrices, mouseX, mouseY, delta);
        } else {
            // TODO: draw "no valid spikes" text
        }

        this.drawCenteredText(
                matrices, this.textRenderer, this.title, this.width / 2, 5, Formatting.WHITE.getColorValue());
        this.drawCenteredText(
                matrices, this.textRenderer, this.subtitle, this.width / 2, 16, Formatting.GRAY.getColorValue());

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void initPageWidgets(boolean newPage) {
        if (this.frameSlider != null) {
            this.buttons.remove(this.frameSlider);
            this.children.remove(this.frameSlider);
        }

        List<PreemptiveReading> spikes = this.analysisResult.valid.getReadings();

        int pieChartRightX = this.width / 2
                + PieChartWidget.calculateWidth(this.client.getWindow().getScaleFactor()) / 2;

        int frame = newPage ? -1 : this.page.getActiveFrame();
        this.page = new CaptureAnalysisPageWidget(this, spikes.get(this.pageNumber));
        this.page.setActiveFrame(frame);
        this.frameSlider = new FrameSliderWidget(
                pieChartRightX + 20,
                this.height / 2 - 10,
                150,
                20,
                value -> this.page.setActiveFrame(value),
                spikes.get(this.pageNumber),
                frame);
        this.addButton(this.frameSlider);
    }
}
