package com.tesselslate.pastry.mixin.cullvis.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.cullvis.CullState;
import com.tesselslate.pastry.mixin.accessor.sodium.ChunkGraphNodeAccessor;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.jellysquid.mods.sodium.client.render.chunk.cull.graph.ChunkGraphCuller;
import me.jellysquid.mods.sodium.client.render.chunk.cull.graph.ChunkGraphIterationQueue;
import me.jellysquid.mods.sodium.client.render.chunk.cull.graph.ChunkGraphNode;
import me.jellysquid.mods.sodium.client.util.math.FrustumExtended;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

@Mixin(value = ChunkGraphCuller.class, remap = false)
public abstract class ChunkGraphCullerMixin {
    @Inject(at = @At("HEAD"), method = "computeVisible(Lnet/minecraft/client/render/Camera;Lme/jellysquid/mods/sodium/client/util/math/FrustumExtended;IZ)Lit/unimi/dsi/fastutil/ints/IntArrayList;", remap = true)
    private void computeVisible_resetCullingState(Camera camera, FrustumExtended frustum, int frame,
            boolean spectator, CallbackInfoReturnable<IntArrayList> cir) {
        Pastry.CURRENT_CULLING_STATE = new CullState();
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphIterationQueue;getNode(I)Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphNode;", remap = false), method = "computeVisible(Lnet/minecraft/client/render/Camera;Lme/jellysquid/mods/sodium/client/util/math/FrustumExtended;IZ)Lit/unimi/dsi/fastutil/ints/IntArrayList;", remap = true)
    private ChunkGraphNode computeVisible_addNodeData(ChunkGraphIterationQueue queue, int index,
            Operation<ChunkGraphNode> orig) {
        ChunkGraphNode node = orig.call(queue, index);

        CullState.Subchunk subchunk = Pastry.CURRENT_CULLING_STATE
                .get(new Vec3i(node.getChunkX(), node.getChunkY(), node.getChunkZ()));

        subchunk.cullingState = node.getCullingState();
        subchunk.visibilityData = ((ChunkGraphNodeAccessor) node).getVisibilityData();

        return node;
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphIterationQueue;add(Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphNode;Lnet/minecraft/util/math/Direction;)V", remap = true), method = "initSearch(Lnet/minecraft/client/render/Camera;Lme/jellysquid/mods/sodium/client/util/math/FrustumExtended;IZ)V", remap = true)
    private void initSearch_markVisible(ChunkGraphIterationQueue queue, ChunkGraphNode node, Direction direction,
            Operation<Void> orig) {
        Pastry.CURRENT_CULLING_STATE.markVisible(new Vec3i(node.getChunkX(), node.getChunkY(), node.getChunkZ()));

        orig.call(queue, node, direction);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphIterationQueue;add(Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphNode;Lnet/minecraft/util/math/Direction;)V", remap = true), method = "bfsEnqueue(Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphNode;Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphNode;Lnet/minecraft/util/math/Direction;)V", remap = true)
    private void bfsEnqueue_markVisible(ChunkGraphIterationQueue queue, ChunkGraphNode node, Direction flow,
            Operation<Void> orig) {
        Vec3i pos = new Vec3i(node.getChunkX(), node.getChunkY(), node.getChunkZ());
        Pastry.CURRENT_CULLING_STATE.markVisible(pos);

        orig.call(queue, node, flow);
    }

    @Inject(at = @At("TAIL"), method = "bfsEnqueue(Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphNode;Lme/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphNode;Lnet/minecraft/util/math/Direction;)V", remap = true)
    private void bfsEnqueue_markFlow(ChunkGraphNode parent, ChunkGraphNode node, Direction flow, CallbackInfo ci) {
        CullState.Subchunk subchunk = Pastry.CURRENT_CULLING_STATE
                .get(new Vec3i(node.getChunkX(), node.getChunkY(), node.getChunkZ()));

        subchunk.flowDirections[flow.getId()] = true;
    }
}
