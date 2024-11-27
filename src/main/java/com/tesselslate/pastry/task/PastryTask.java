package com.tesselslate.pastry.task;

import com.tesselslate.pastry.Pastry;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.RecursiveTask;

import org.jetbrains.annotations.NotNull;

abstract class PastryTask<V> extends RecursiveTask<Exceptional<V>> {
    @NotNull
    private final String name;

    public PastryTask(@NotNull String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    protected abstract V runTask() throws Exception;

    @Override
    protected Exceptional<V> compute() {
        try {
            Instant start = Instant.now();
            V value = this.runTask();
            Instant end = Instant.now();

            Duration spent = Duration.between(start, end);
            long seconds = spent.getSeconds();
            long micros = spent.getNano() / 1000L;
            Pastry.LOGGER.info(String.format("Completed %s in %d.%06d sec", this.name, seconds, micros));

            return new Exceptional<>(value);
        } catch (Exception e) {
            return new Exceptional<>(e);
        }
    }
}
