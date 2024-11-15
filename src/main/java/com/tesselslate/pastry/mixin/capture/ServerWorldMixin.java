package com.tesselslate.pastry.mixin.capture;

import org.spongepowered.asm.mixin.Mixin;

import com.tesselslate.pastry.capture.structure.PastryStructureCache;
import com.tesselslate.pastry.interfaces.PastryServerWorld;

import net.minecraft.server.world.ServerWorld;

@Mixin(value = ServerWorld.class)
public abstract class ServerWorldMixin implements PastryServerWorld {
    private PastryStructureCache structureCache;

    @Override
    public PastryStructureCache pastry$getStructureCache() {
        if (this.structureCache == null) {
            this.structureCache = new PastryStructureCache((ServerWorld) (Object) this);
        }

        return this.structureCache;
    }
}
