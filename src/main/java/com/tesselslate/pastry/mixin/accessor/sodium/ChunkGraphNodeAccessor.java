package com.tesselslate.pastry.mixin.accessor.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.render.chunk.cull.graph.ChunkGraphNode;

@Mixin(value = ChunkGraphNode.class, remap = false)
public interface ChunkGraphNodeAccessor {
    @Accessor("visibilityData")
    public long getVisibilityData();
}
