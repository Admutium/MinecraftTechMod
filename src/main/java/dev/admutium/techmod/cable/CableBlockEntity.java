package dev.admutium.techmod.cable;

import com.mojang.serialization.Codec;
import dev.admutium.techmod.TechMod;
import net.fabricmc.fabric.api.blockgetter.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

public final class CableBlockEntity extends BlockEntity implements RenderDataBlockEntity {
    private volatile CableSnapshot data = CableSnapshot.EMPTY;
    private volatile CableSnapshot renderData = CableSnapshot.EMPTY;
    private final CableShapeCache<net.minecraft.world.phys.shapes.VoxelShape> shapeCache = new CableShapeCache<>();
    private int cachedSnow=-1;
    public net.minecraft.world.phys.shapes.VoxelShape selectionShape(net.minecraft.world.level.BlockGetter world) {
        int layers=getBlockState().getValue(CableBlock.SNOW);if(cachedSnow!=layers){shapeCache.invalidate();cachedSnow=layers;}
        return shapeCache.get(() -> {var shape=CableShapes.build(world,worldPosition,renderData);return layers==0?shape:net.minecraft.world.phys.shapes.Shapes.joinUnoptimized(shape,net.minecraft.world.phys.shapes.Shapes.box(0,0,0,1,layers/8.0,1),net.minecraft.world.phys.shapes.BooleanOp.OR);});
    }
    private void invalidateShapes() {
        shapeCache.invalidate();
        if (level==null) return;
        for (BlockPos p:BlockPos.betweenClosed(worldPosition.offset(-1,-1,-1),worldPosition.offset(1,1,1)))
            if (level.hasChunkAt(p) && level.getChunkAt(p).getBlockEntities().get(p) instanceof CableBlockEntity be)
                be.shapeCache.invalidate();
    }
    @Override public void setRemoved() {
        invalidateShapes();
        if(level!=null)dev.admutium.techmod.energy.EnergyNetwork.invalidate(level);
        super.setRemoved();
    }

    public CableBlockEntity(BlockPos pos, BlockState state) { super(TechMod.CABLE_ENTITY, pos, state); }
    public CableSnapshot snapshot() { return data; }
    @Override public CableSnapshot getRenderData() { return renderData; }
    public void update(CableSnapshot next) {
        if(level!=null)dev.admutium.techmod.energy.EnergyNetwork.invalidate(level);
        data = next;
        renderData = next.centered();
        invalidateShapes();
        setChanged();
        if (level instanceof ServerLevel server) {
            server.getChunkSource().blockChanged(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            // Connections can change across an edge, including diagonally adjacent faces.
            for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 1, 1))) {
                if (level.hasChunkAt(p) && level.getBlockState(p).is(TechMod.CABLE))
                    level.sendBlockUpdated(p, level.getBlockState(p), level.getBlockState(p), 3);
            }
        }
    }
    @Override public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        var spans = new ArrayList<CableSpan>();
        for (String encoded : input.read("spans", Codec.STRING.listOf()).orElseGet(java.util.List::of)) {
            try { spans.add(CableSpan.decode(encoded)); }
            catch (RuntimeException e) { LoggerFactory.getLogger("techmod").warn("Ignoring invalid saved cable span at {}", worldPosition); }
        }
        data = new CableSnapshot(input.getLongOr("wires", 0) & ((1L << 54) - 1), input.getIntOr("holders", 0) & 63, spans);
        renderData=data.centered();
        invalidateShapes();
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 1, 1)))
                level.sendBlockUpdated(p, level.getBlockState(p), level.getBlockState(p), 3);
        }
    }
    @Override public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("wires", data.wires());
        output.putInt("holders", data.holders());
        output.store("spans", Codec.STRING.listOf(), data.spans().stream().map(CableSpan::encode).toList());
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveCustomOnly(registries); }
}
