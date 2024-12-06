package com.tesselslate.pastry.analysis.preemptive.conditions;

import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public abstract class EntityCondition implements Condition {
    protected final String entity;

    protected EntityCondition(String entity) {
        this.entity = entity;
    }

    /**
     * Returns the translated name of the entity associated with this condition, if
     * any.
     *
     * @return The translated name of the entity associated with this condition
     */
    protected String getEntityName() {
        EntityType<?> type = Registry.ENTITY_TYPE.get(new Identifier(this.entity));
        return type.getName().getString();
    }
}
