package com.tesselslate.pastry.gui.widget;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveReading;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;
import java.util.function.Consumer;

public class FrameSliderWidget extends SliderWidget {
    public static final int AVERAGE = -1;

    private final PreemptiveReading reading;
    private final Consumer<Integer> onChange;

    private int frame;

    public FrameSliderWidget(
            int x, int y, int w, int h, Consumer<Integer> onChange, PreemptiveReading reading, int frame) {
        super(x, y, w, h, LiteralText.EMPTY, calculateValue(w, frame, reading));

        this.reading = Objects.requireNonNull(reading);
        this.onChange = Objects.requireNonNull(onChange);

        this.updateFrame();
        this.updateMessage();
    }

    @Override
    protected void applyValue() {
        this.updateFrame();
        this.onChange.accept(this.frame - 1);
    }

    @Override
    protected void updateMessage() {
        if (this.frame == 0) {
            this.setMessage(new LiteralText("Average"));
        } else {
            String text = String.format("Frame: %d/%d", this.frame, this.reading.frames().length);
            this.setMessage(new LiteralText(text));
        }
    }

    private void updateFrame() {
        int frame = (int) (this.value * (double) this.reading.frames().length);
        this.frame = MathHelper.clamp(frame, 0, this.reading.frames().length);
    }

    private static double calculateValue(int width, int frame, PreemptiveReading reading) {
        return (double) (frame + 1) / (double) reading.frames().length;
    }
}
