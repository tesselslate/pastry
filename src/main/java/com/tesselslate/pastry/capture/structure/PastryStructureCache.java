package com.tesselslate.pastry.capture.structure;

import java.util.HashMap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.feature.StructureFeature;

/**
 * Finds structures within a given radius of the player.
 */
public class PastryStructureCache {
    private StructureAccessor accessor;

    private HashMap<Box, PastryCaptureStructure> strongholds;

    public PastryStructureCache(ServerWorld world) {
        this.accessor = world.getStructureAccessor();

        this.strongholds = new HashMap<>();
    }

    /**
     * Attempts to locate a stronghold which contains the {@code player}.
     *
     * @param player The player
     * @return A stronghold colliding with the player, if one exists
     */
    public PastryCaptureStructure findStronghold(ServerPlayerEntity player) {
        return this.findStructure(StructureFeature.STRONGHOLD, player);
    }

    /**
     * Attempts to locate a structure of the given type which contains the
     * {@code player}.
     *
     * @param feature The type of structure start to search for
     * @param player  The player
     * @return A structure of the given type colliding with the player, if one
     *         exists
     */
    private PastryCaptureStructure findStructure(StructureFeature<?> feature, ServerPlayerEntity player) {
        for (Box box : this.strongholds.keySet()) {
            if (box.intersects(player.getBoundingBox())) {
                return this.strongholds.get(box);
            }
        }

        StructureStart<?> start = this.accessor.getStructureAt(new BlockPos(player.getPos()), true, feature);
        if (start == null || start.getFeature() != feature) {
            // HACK: The game sometimes returns default mineshaft structures for no apparent
            // reason. Ignore the returned structure if it does not have the correct feature
            // type.
            return null;
        }

        BlockBox bbox = start.getBoundingBox();
        Box box = new Box(bbox.minX, bbox.minY, bbox.minZ, bbox.maxX + 1.0, bbox.maxY + 1.0, bbox.maxZ + 1.0);

        PastryCaptureStructure pastryStructure = new PastryCaptureStructure(start);
        this.strongholds.put(box, pastryStructure);
        return pastryStructure;
    }
}
