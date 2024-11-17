package com.tesselslate.pastry.gui.widget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;

public class PaginatedListWidget<E extends PaginatedListWidget.Entry<E>> extends ElementListWidget<E> {
    private static final String LEFT_ARROW = "\u25c0";
    private static final String RIGHT_ARROW = "\u25b6";

    private final Screen parent;

    private final int entriesPerPage;
    private final List<E> entries;

    private ButtonWidget nextPageButton;
    private ButtonWidget prevPageButton;

    private int page = -1;

    public PaginatedListWidget(Screen screen, MinecraftClient client, int width, int height, int top, int bottom,
            int rowHeight,
            int entriesPerPage, List<E> entries, int page) {
        super(client, width, height, top, bottom, rowHeight);

        this.parent = screen;

        this.entriesPerPage = entriesPerPage;
        this.entries = new ArrayList<>(entries);

        this.switchPage(page);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.nextPageButton != null) {
            this.nextPageButton.render(matrices, mouseX, mouseY, delta);
        }
        if (this.prevPageButton != null) {
            this.prevPageButton.render(matrices, mouseX, mouseY, delta);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    public ButtonWidget createNextPageButton(int x, int y, int w, int h) {
        this.nextPageButton = new ButtonWidget(x, y, w, h, new LiteralText(RIGHT_ARROW), button -> {
            this.nextPage();
        }, (button, matrices, mouseX, mouseY) -> {
            if (button.active) {
                this.parent.renderTooltip(matrices,
                        StringRenderable.plain(String.format("Page %d/%d", this.page + 2, this.getPageCount())), mouseX,
                        mouseY);
            }
        });

        this.nextPageButton.active = this.nextPageExists();
        return this.nextPageButton;
    }

    public ButtonWidget createPrevPageButton(int x, int y, int w, int h) {
        this.prevPageButton = new ButtonWidget(x, y, w, h, new LiteralText(LEFT_ARROW), button -> {
            this.prevPage();
        }, (button, matrices, mouseX, mouseY) -> {
            if (button.active) {
                this.parent.renderTooltip(matrices,
                        StringRenderable.plain(String.format("Page %d/%d", this.page, this.getPageCount())), mouseX,
                        mouseY);
            }
        });

        this.prevPageButton.active = this.prevPageExists();
        return this.prevPageButton;
    }

    public int getPageCount() {
        return (int) Math.ceil((double) this.entries.size() / (double) this.entriesPerPage);
    }

    public int getPage() {
        return this.page;
    }

    public boolean nextPageExists() {
        return this.page < getPageCount() - 1;
    }

    public void nextPage() {
        if (this.nextPageExists()) {
            this.switchPage(this.page + 1);
        }
    }

    public boolean prevPageExists() {
        return this.page > 0;
    }

    public void prevPage() {
        if (this.prevPageExists()) {
            this.switchPage(this.page - 1);
        }
    }

    public void switchPage(int page) {
        assert page >= 0;

        if (this.page == page) {
            return;
        }

        this.page = page;
        assert this.page * this.entriesPerPage <= this.entries.size();

        List<E> pageEntries = this.entries.subList(this.page * this.entriesPerPage,
                Math.min(this.entries.size(), (this.page + 1) * this.entriesPerPage));

        this.clearEntries();
        for (E entry : pageEntries) {
            this.addEntry(entry);
        }

        if (this.nextPageButton != null) {
            this.nextPageButton.active = this.nextPageExists();
        }
        if (this.prevPageButton != null) {
            this.prevPageButton.active = this.prevPageExists();
        }
    }
}
