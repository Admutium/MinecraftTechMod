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

public final class CableBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty SNOW=net.minecraft.world.level.block.state.properties.IntegerProperty.create("snow",0,7);
    public CableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false).setValue(SNOW,0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(WATERLOGGED,SNOW); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CableBlockEntity(pos, state); }
    @Override public boolean placeLiquid(net.minecraft.world.level.LevelAccessor level,BlockPos pos,BlockState state,FluidState fluid){
        boolean placed=SimpleWaterloggedBlock.super.placeLiquid(level,pos,state,fluid);
        if(placed&&!level.isClientSide()&&state.getValue(SNOW)>0)level.setBlock(pos,level.getBlockState(pos).setValue(SNOW,0),UPDATE_ALL);
        return placed;
    }
    @Override public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity entity)) return Shapes.empty();
        return entity.selectionShape(level);
    }
    @Override protected net.minecraft.world.InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState state,Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){
        if(stack.is(net.minecraft.world.item.Items.SNOW)){
            if(!player.mayBuild()||!level.mayInteract(player,pos)||!player.mayUseItemAt(pos,hit.getDirection(),stack))return net.minecraft.world.InteractionResult.FAIL;
            if(state.getValue(WATERLOGGED)||state.getValue(SNOW)>=7||!net.minecraft.world.level.block.Blocks.SNOW.defaultBlockState().canSurvive(level,pos))return net.minecraft.world.InteractionResult.FAIL;
            if(!level.isClientSide()){level.setBlock(pos,state.setValue(SNOW,state.getValue(SNOW)+1),UPDATE_ALL);stack.consume(1,player);}
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack,state,level,pos,player,hand,hit);
    }
    @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.phys.BlockHitResult hit){
        if(player.isShiftKeyDown()&&player.getMainHandItem().isEmpty())return TechMod.WIRE_CUTTERS.useOn(new net.minecraft.world.item.context.UseOnContext(player,net.minecraft.world.InteractionHand.MAIN_HAND,hit));
        return net.minecraft.world.InteractionResult.PASS;
    }
    @Override public VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        int layers=state.getValue(SNOW);return layers<=1?Shapes.empty():Shapes.box(0,0,0,1,(layers-1)/8.0,1);
    }
    @Override protected void randomTick(BlockState state,net.minecraft.server.level.ServerLevel level,BlockPos pos,net.minecraft.util.RandomSource random){
        if(state.getValue(SNOW)>0&&level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos)>11)level.setBlock(pos,state.setValue(SNOW,0),UPDATE_ALL);
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TechMod.CABLE_ENTITY) return null;
        return (world, pos, blockState, entity) -> {
            if ((world.getGameTime() + pos.asLong()) % 20 == 0)
                CableWorld.validate(world, pos, (CableBlockEntity) entity);
        };
    }
}
