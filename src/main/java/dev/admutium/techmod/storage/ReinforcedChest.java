package dev.admutium.techmod.storage;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import java.util.List;
public final class ReinforcedChest extends Block implements EntityBlock {
 public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final BooleanProperty OPEN=BooleanProperty.create("open");
 public ReinforcedChest(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(OPEN,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,OPEN);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ReinforcedChestEntity(p,s);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> type){return !w.isClientSide()&&type==ReinforcedStorage.ENTITY?(world,p,state,be)->{if(world.getGameTime()%20==0)((ReinforcedChestEntity)be).closeIfUnused();}:null;}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return Block.box(1,0,1,15,15,15);}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){
  if(w.getBlockState(p.above()).isRedstoneConductor(w,p.above()))return InteractionResult.SUCCESS;
  if(!w.isClientSide()&&w.getBlockEntity(p) instanceof ReinforcedChestEntity be)player.openMenu(be);return InteractionResult.SUCCESS;
 }
 @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder params){return List.of(new ItemStack(asItem()));}
 @Override protected boolean hasAnalogOutputSignal(BlockState s){return true;}
 @Override protected int getAnalogOutputSignal(BlockState s,Level w,BlockPos p,Direction d){return net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromBlockEntity(w.getBlockEntity(p));}
}
