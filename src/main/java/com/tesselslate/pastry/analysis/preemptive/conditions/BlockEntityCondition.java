package com.tesselslate.pastry.analysis.preemptive.conditions;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public abstract class BlockEntityCondition implements Condition {
    protected final String blockEntity;

    protected BlockEntityCondition(String blockEntity) {
        this.blockEntity = blockEntity;
    }

    /**
     * Returns the translated name of the block entity associated with this
     * condition.
     *
     * @return The translated name of the block entity associated with this
     *         condition
     */
    protected String getBlockEntityName() {
        Block block = Registry.BLOCK.get(new Identifier(this.blockEntity));
        return block.getName().getString();
    }
}
