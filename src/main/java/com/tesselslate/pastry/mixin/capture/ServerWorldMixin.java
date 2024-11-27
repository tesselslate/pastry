package com.tesselslate.pastry.mixin.capture;

import com.tesselslate.pastry.capture.structure.PastryStructureCache;
import com.tesselslate.pastry.interfaces.PastryServerWorld;

import net.minecraft.server.world.ServerWorld;

import org.spongepowered.asm.mixin.Mixin;

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
