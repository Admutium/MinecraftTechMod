package dev.admutium.techmod.cable;

import dev.admutium.techmod.TechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.ArrayList;
import java.util.List;

public final class CableBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public CableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(WATERLOGGED); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CableBlockEntity(pos, state); }
    @Override public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity entity)) return Shapes.empty();
        VoxelShape shape = Shapes.empty();
        CableSnapshot snapshot = entity.snapshot();
        Vec origin = new Vec(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < 54; i++) {
            Node n = Node.fromIndex(pos.getX(), pos.getY(), pos.getZ(), i);
            if (snapshot.has(n) || (n.u() == 1 && n.v() == 1 && snapshot.holder(n.face()))) {
                Vec p = n.point().subtract(origin);
                double r = snapshot.holder(n.face()) && n.u() == 1 && n.v() == 1 ? .13 : .12;
                shape = Shapes.or(shape, Shapes.box(Math.max(0, p.x()-r), Math.max(0, p.y()-r), Math.max(0, p.z()-r),
                        Math.min(1, p.x()+r), Math.min(1, p.y()+r), Math.min(1, p.z()+r)));
                var links = SurfaceTopology.connections(n, other -> {
                    BlockPos target = new BlockPos(other.x(), other.y(), other.z());
                    if (!(level.getBlockEntity(target) instanceof CableBlockEntity cable)) return false;
                    return cable.snapshot().has(other) || (other.u()==1 && other.v()==1 && cable.snapshot().holder(other.face()));
                });
                List<List<Vec>> paths = new ArrayList<>();
                for (var link : links) paths.add(link.path());
                if (links.isEmpty() && snapshot.has(n)) paths.add(SurfaceTopology.coil(n));
                for (List<Vec> path : paths) for (int j=1; j<path.size(); j++) {
                    Vec a=path.get(j-1), b=path.get(j);
                    int steps=Math.max(1,(int)Math.ceil(b.subtract(a).length()/.045));
                    for (int k=0;k<=steps;k++) {
                        Vec point=a.add(b.subtract(a).scale((double)k/steps)).subtract(origin);
                        shape=addSample(shape,point,Node.RADIUS+.009);
                    }
                }
            }
        }
        for (CableSpan span : snapshot.spans()) {
            for (Vec world : span.points()) {
                Vec p = world.subtract(origin);
                double r = Node.RADIUS;
                if (p.x() >= 0 && p.x() <= 1 && p.y() >= 0 && p.y() <= 1 && p.z() >= 0 && p.z() <= 1)
                    shape = Shapes.or(shape, Shapes.box(Math.max(0,p.x()-r), Math.max(0,p.y()-r), Math.max(0,p.z()-r),
                            Math.min(1,p.x()+r), Math.min(1,p.y()+r), Math.min(1,p.z()+r)));
            }
        }
        return shape;
    }
    private static VoxelShape addSample(VoxelShape shape, Vec p, double r) {
        double minX=Math.max(0,p.x()-r), minY=Math.max(0,p.y()-r), minZ=Math.max(0,p.z()-r);
        double maxX=Math.min(1,p.x()+r), maxY=Math.min(1,p.y()+r), maxZ=Math.min(1,p.z()+r);
        if (minX>=maxX || minY>=maxY || minZ>=maxZ) return shape;
        return Shapes.or(shape,Shapes.box(minX,minY,minZ,maxX,maxY,maxZ));
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TechMod.CABLE_ENTITY) return null;
        return (world, pos, blockState, entity) -> {
            if ((world.getGameTime() + pos.asLong()) % 20 == 0)
                CableWorld.validate(world, pos, (CableBlockEntity) entity);
        };
    }
}
