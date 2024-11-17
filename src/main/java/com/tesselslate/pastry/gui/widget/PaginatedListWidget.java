package com.tesselslate.pastry.gui.widget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;

public class PaginatedListWidget<E extends PaginatedListWidget.Entry<E>> extends ElementListWidget<E> {
    private final int entriesPerPage;
    private final List<E> entries;

    private int page = -1;

    public PaginatedListWidget(MinecraftClient client, int width, int height, int top, int bottom, int rowHeight,
            int entriesPerPage, List<E> entries) {
        super(client, width, height, top, bottom, rowHeight);

        this.entriesPerPage = entriesPerPage;
        this.entries = new ArrayList<>(entries);

        this.switchPage(0);
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
    }
}
