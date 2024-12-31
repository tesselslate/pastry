package com.tesselslate.pastry.gui.screen;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveAnalysis;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveAnalysisResult;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveSpikes;
import com.tesselslate.pastry.capture.PastryCaptureHeader;
import com.tesselslate.pastry.gui.ScreenExtended;
import com.tesselslate.pastry.gui.widget.CaptureAnalysisPageWidget;
import com.tesselslate.pastry.gui.widget.FrameSliderWidget;
import com.tesselslate.pastry.gui.widget.PieChartWidget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.util.Formatting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CaptureAnalysisScreen extends ScreenExtended {
    private static final String LEFT_ARROW = "\u25c0";
    private static final String RIGHT_ARROW = "\u25b6";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private PreemptiveAnalysisResult analysisResult;

    private final LiteralText subtitle;

    private CaptureAnalysisPageWidget page;
    private ButtonWidget nextPageButton;
    private ButtonWidget prevPageButton;
    private FrameSliderWidget frameSlider;

    private ButtonWidget toggleFilterModeButton;
    private FilterMode activeFilterMode;

    private PreemptiveSpikes spikes;
    private int pageNumber;

    public CaptureAnalysisScreen(Screen parent, PastryCaptureHeader header, PreemptiveAnalysis analysis) {
        super(parent, new LiteralText("Analysis of " + DATE_FORMAT.format(header.recordedAt)));

        this.analysisResult = analysis.process();

        int valid = this.analysisResult.valid.getReadings().size();
        int invalid = this.analysisResult.invalid.getReadings().size();
        this.subtitle = new LiteralText(String.format("%d/%d valid readings", valid, valid + invalid));

        this.activeFilterMode = FilterMode.VALID;
        this.spikes = this.analysisResult.valid;
    }

    public CaptureAnalysisScreen(Screen parent, PreemptiveAnalysis analysis) {
        super(parent, new LiteralText("Analysis"));

        this.analysisResult = analysis.process();

        int valid = this.analysisResult.valid.getReadings().size();
        int invalid = this.analysisResult.invalid.getReadings().size();
        this.subtitle = new LiteralText(String.format("%d/%d valid readings", valid, valid + invalid));

        this.activeFilterMode = FilterMode.VALID;
        this.spikes = this.analysisResult.valid;
    }

    @Override
    protected void init() {
        if (this.spikes.size() > 0) {
            this.initPageWidgets(this.page == null);
        }

        this.addButton(this.createNextPageButton(this.width / 2 + 104, this.height - 27, 20, 20));
        this.addButton(this.createPrevPageButton(this.width / 2 - 124, this.height - 27, 20, 20));
        this.addButton(this.createDoneButton(this.width / 2 - 100, this.height - 27, 200, 20));

        int pieChartRightX = this.width / 2
                + PieChartWidget.calculateWidth(this.client.getWindow().getScaleFactor()) / 2;
        this.addButton(this.createToggleButton(pieChartRightX + 20, this.height / 2 + 2, 150, 20));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (this.page != null) {
            this.page.render(matrices, mouseX, mouseY, delta);
        } else {
            this.drawCenteredText(
                    matrices,
                    this.textRenderer,
                    StringRenderable.plain("No readings to show"),
                    this.width / 2,
                    this.height / 2 - this.textRenderer.fontHeight / 2,
                    Formatting.WHITE.getColorValue());
        }

        this.drawCenteredText(
                matrices, this.textRenderer, this.title, this.width / 2, 5, Formatting.WHITE.getColorValue());
        this.drawCenteredText(
                matrices, this.textRenderer, this.subtitle, this.width / 2, 16, Formatting.GRAY.getColorValue());

        super.render(matrices, mouseX, mouseY, delta);
    }

    private ButtonWidget createNextPageButton(int x, int y, int w, int h) {
        this.nextPageButton = new ButtonWidget(
                x,
                y,
                w,
                h,
                new LiteralText(RIGHT_ARROW),
                button -> {
                    if (this.pageNumber < this.spikes.size() - 1) {
                        this.setPage(this.pageNumber + 1);
                    }
                },
                (button, matrices, mouseX, mouseY) -> {
                    if (button.active) {
                        this.parent.renderTooltip(
                                matrices,
                                StringRenderable.plain(
                                        String.format("Page %d/%d", this.pageNumber + 2, this.spikes.size())),
                                mouseX,
                                mouseY);
                    }
                });

        this.nextPageButton.active = this.pageNumber < this.spikes.size() - 1;

        return this.nextPageButton;
    }

    private ButtonWidget createPrevPageButton(int x, int y, int w, int h) {
        this.prevPageButton = new ButtonWidget(
                x,
                y,
                w,
                h,
                new LiteralText(LEFT_ARROW),
                button -> {
                    if (this.pageNumber > 0) {
                        this.setPage(this.pageNumber - 1);
                    }
                },
                (button, matrices, mouseX, mouseY) -> {
                    if (button.active) {
                        this.parent.renderTooltip(
                                matrices,
                                StringRenderable.plain(
                                        String.format("Page %d/%d", this.pageNumber, this.spikes.size())),
                                mouseX,
                                mouseY);
                    }
                });

        this.prevPageButton.active = this.pageNumber > 0;

        return this.prevPageButton;
    }

    private ButtonWidget createToggleButton(int x, int y, int w, int h) {
        this.toggleFilterModeButton = new ButtonWidget(
                x,
                y,
                w,
                h,
                new LiteralText(this.activeFilterMode.next().toString()),
                button -> this.setFilterMode(this.activeFilterMode.next()));

        return this.toggleFilterModeButton;
    }

    private void initPageWidgets(boolean newPage) {
        if (this.frameSlider != null) {
            this.buttons.remove(this.frameSlider);
            this.children.remove(this.frameSlider);
        }

        if (this.spikes.empty()) {
            this.page = null;
            return;
        }

        int pieChartRightX = this.width / 2
                + PieChartWidget.calculateWidth(this.client.getWindow().getScaleFactor()) / 2;

        int frame = newPage ? -1 : this.page.getActiveFrame();
        this.page = new CaptureAnalysisPageWidget(this, this.spikes.get(this.pageNumber));
        this.page.setActiveFrame(frame);
        this.frameSlider = new FrameSliderWidget(
                pieChartRightX + 20,
                this.height / 2 - 22,
                150,
                20,
                value -> this.page.setActiveFrame(value),
                this.spikes.get(this.pageNumber),
                frame);
        this.addButton(this.frameSlider);
    }

    private void setFilterMode(FilterMode mode) {
        this.activeFilterMode = mode;

        if (this.toggleFilterModeButton != null) {
            this.toggleFilterModeButton.setMessage(new LiteralText(this.activeFilterMode.toString()));
        }

        switch (this.activeFilterMode) {
            case FilterMode.VALID:
                this.spikes = this.analysisResult.valid;
                break;
            case FilterMode.INVALID:
                this.spikes = this.analysisResult.invalid;
                break;
        }

        this.setPage(0);
    }

    private void setPage(int page) {
        this.pageNumber = page;

        if (this.nextPageButton != null) {
            this.nextPageButton.active = this.pageNumber < this.spikes.size() - 1;
            this.nextPageButton.active &= !this.spikes.empty();
        }
        if (this.prevPageButton != null) {
            this.prevPageButton.active = this.pageNumber > 0;
            this.nextPageButton.active &= !this.spikes.empty();
        }

        initPageWidgets(true);
    }

    private enum FilterMode {
        VALID,
        INVALID;

        private FilterMode next() {
            switch (this) {
                case VALID:
                    return INVALID;
                case INVALID:
                    return VALID;
            }

            throw new RuntimeException("Unreachable");
        }

        @Override
        public String toString() {
            switch (this) {
                case VALID:
                    return "Valid";
                case INVALID:
                    return "Invalid";
            }

            throw new RuntimeException("Unreachable");
        }
    }
}
