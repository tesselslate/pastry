package com.tesselslate.pastry.analysis.preemptive.conditions;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveReading;

public final class EntityConditions {
    public static Condition entityCountEquals(int target) {
        return new Condition() {
            @Override
            public String getTooltip() {
                return String.format("E = %d", target);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.entities().size() == target;
            }
        };
    }

    public static Condition entityCountEquals(int target, String entity) {
        return new EntityCondition(entity) {
            @Override
            public String getTooltip() {
                return String.format("%s = %d", this.getEntityName(), target);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.entities().stream()
                                .filter(entity -> entity.name.equals(this.entity))
                                .count()
                        == target;
            }
        };
    }

    public static Condition entityCountMax(int threshold) {
        return new Condition() {
            @Override
            public String getTooltip() {
                return String.format("E <= %d", threshold);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.entities().size() <= threshold;
            }
        };
    }

    public static Condition entityCountMax(int threshold, String entity) {
        return new EntityCondition(entity) {
            @Override
            public String getTooltip() {
                return String.format("%s <= %d", this.getEntityName(), threshold);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.entities().stream()
                                .filter(entity -> entity.name.equals(this.entity))
                                .count()
                        <= threshold;
            }
        };
    }

    public static Condition entityCountMin(int threshold) {
        return new Condition() {
            @Override
            public String getTooltip() {
                return String.format("E >= %d", threshold);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.entities().size() >= threshold;
            }
        };
    }

    public static Condition entityCountMin(int threshold, String entity) {
        return new EntityCondition(entity) {
            @Override
            public String getTooltip() {
                return String.format("%s >= %d", this.getEntityName(), threshold);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.entities().stream()
                                .filter(entity -> entity.name.equals(this.entity))
                                .count()
                        >= threshold;
            }
        };
    }
}
