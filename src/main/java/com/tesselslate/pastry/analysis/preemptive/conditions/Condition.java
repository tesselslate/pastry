package com.tesselslate.pastry.analysis.preemptive.conditions;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveReading;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

public interface Condition extends Predicate<PreemptiveReading> {
    /**
     * Returns the tooltip to display for this condition.
     *
     * @return The tooltip to display for this condition
     */
    public @Nullable String getTooltip();
}
