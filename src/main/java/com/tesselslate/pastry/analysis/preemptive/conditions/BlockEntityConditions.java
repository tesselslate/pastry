package com.tesselslate.pastry.analysis.preemptive.conditions;

import com.tesselslate.pastry.analysis.preemptive.PreemptiveReading;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class BlockEntityConditions {
    public static Condition blockEntityCountEquals(int target) {
        return new Condition() {
            @Override
            public String getTooltip() {
                return String.format("BE = %d", target);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.blockEntities().size() == target;
            }
        };
    }

    public static Condition blockEntityCountEquals(int target, String blockEntity) {
        return new BlockEntityCondition(blockEntity) {
            @Override
            public String getTooltip() {
                return String.format("%s = %d", this.getBlockEntityName(), target);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.blockEntities().stream()
                                .filter(blockEntity -> blockEntity.name.equals(this.blockEntity))
                                .count()
                        == target;
            }
        };
    }

    public static Condition blockEntityCountMax(int threshold) {
        return new Condition() {
            @Override
            public String getTooltip() {
                return String.format("BE <= %d", threshold);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.blockEntities().size() <= threshold;
            }
        };
    }

    public static Condition blockEntityCountMax(int threshold, String blockEntity) {
        return new BlockEntityCondition(blockEntity) {
            @Override
            public String getTooltip() {
                return String.format("%s <= %d", this.getBlockEntityName(), threshold);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.blockEntities().stream()
                                .filter(blockEntity -> blockEntity.name.equals(this.blockEntity))
                                .count()
                        <= threshold;
            }
        };
    }

    public static Condition blockEntityCountMin(int threshold) {
        return new Condition() {
            @Override
            public String getTooltip() {
                return String.format("BE >= %d", threshold);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.blockEntities().size() >= threshold;
            }
        };
    }

    public static Condition blockEntityCountMin(int threshold, String blockEntity) {
        return new BlockEntityCondition(blockEntity) {
            @Override
            public String getTooltip() {
                return String.format("%s >= %d", this.getBlockEntityName(), threshold);
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.blockEntities().stream()
                                .filter(blockEntity -> blockEntity.name.equals(this.blockEntity))
                                .count()
                        >= threshold;
            }
        };
    }

    public static Condition hasSpawnerOf(String entity) {
        return new Condition() {
            @Override
            public String getTooltip() {
                return String.format(
                        "Has %s spawner",
                        Registry.ENTITY_TYPE
                                .get(new Identifier(entity))
                                .getName()
                                .getString());
            }

            @Override
            public boolean test(PreemptiveReading reading) {
                return reading.blockEntities().stream()
                        .filter(blockEntity ->
                                blockEntity.name.equals("mob_spawner") && blockEntity.data.equals(entity))
                        .findAny()
                        .isPresent();
            }
        };
    }
}
