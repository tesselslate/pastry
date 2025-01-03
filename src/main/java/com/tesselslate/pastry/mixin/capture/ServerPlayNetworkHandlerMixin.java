package com.tesselslate.pastry.mixin.capture;

import com.tesselslate.pastry.capture.PastryCaptureManager;
import com.tesselslate.pastry.capture.structure.PastryCaptureStructure;
import com.tesselslate.pastry.capture.structure.PastryStructureCache;
import com.tesselslate.pastry.interfaces.PastryServerWorld;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkStatus;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @WrapMethod(method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V")
    private void onPlayerMove_searchStrongholds(PlayerMoveC2SPacket packet, Operation<Void> orig) {
        orig.call(packet);

        ServerPlayerEntity player = ((ServerPlayNetworkHandler) (Object) this).player;
        ServerWorld world = player.getServerWorld();

        // Do not attempt to search for a stronghold unless the player is in a loaded
        // chunk.
        int chunkX = (int) player.getPos().getX() / 16;
        int chunkZ = (int) player.getPos().getZ() / 16;
        if (world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null) {
            return;
        }

        PastryStructureCache structureCache = ((PastryServerWorld) world).pastry$getStructureCache();
        PastryCaptureStructure stronghold = structureCache.findStronghold(player);
        if (stronghold == null) {
            return;
        }

        PastryCaptureManager.update(capture -> capture.addStructure(stronghold));
    }
}
