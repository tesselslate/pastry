package com.tesselslate.pastry.cullvis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.jellysquid.mods.sodium.client.render.chunk.cull.graph.ChunkGraphCuller;
import me.jellysquid.mods.sodium.client.render.chunk.cull.graph.ChunkGraphNode;
import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import net.minecraft.util.math.Vec3i;

/**
 * Contains information about a full run of the chunk culling algorithm.
 *
 * @see ChunkGraphCuller
 */
public class CullState {
    /**
     * Contains information about the culling state of a single subchunk.
     *
     * @see ChunkGraphNode
     */
    public class Subchunk {
        /**
         * The directions through which a culling BFS has flowed.
         */
        public boolean[] flowDirections = new boolean[DirectionUtil.ALL_DIRECTIONS.length];

        /**
         * Which faces of the subchunk can be reached from one another via a
         * continuous path of non-opaque blocks.
         */
        public long visibilityData;

        /**
         * The set of directions which cannot be traversed from this subchunk.
         */
        public byte cullingState;
    }

    /**
     * Contains information about the culling state of individual subchunks.
     */
    public Map<Vec3i, Subchunk> data;

    /**
     * The set of all visible subchunks.
     */
    public Set<Vec3i> visible;

    public CullState() {
        this.data = new HashMap<>();
        this.visible = new HashSet<>();
    }

    /**
     * Returns the {@link Subchunk} at {@code pos} if it exists, or creates a
     * new {@link Subchunk} if it does not.
     *
     * @param pos The position of the {@link Subchunk} to retrieve
     * @return The subchunk at {@code pos}
     */
    public Subchunk get(Vec3i pos) {
        return this.data.computeIfAbsent(pos, key -> new Subchunk());
    }

    /**
     * Marks the subchunk at {@code pos} as visible.
     *
     * @param pos The position of the subchunk to mark visible
     */
    public void markVisible(Vec3i pos) {
        this.visible.add(pos);
    }
}
