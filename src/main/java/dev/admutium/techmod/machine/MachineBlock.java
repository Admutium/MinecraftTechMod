package dev.admutium.techmod.machine;
import dev.admutium.techmod.cable.CablePort;
import dev.admutium.techmod.energy.EnergyNetwork;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;
public final class MachineBlock extends Block implements EntityBlock,CablePort {
 public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final BooleanProperty UPPER=BooleanProperty.create("upper"),ACTIVE=BooleanProperty.create("active");
 public static final IntegerProperty STATUS=IntegerProperty.create("status",0,2);
 public final MachineKind kind;
 public MachineBlock(MachineKind k,Properties p){super(p);kind=k;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(UPPER,false).setValue(ACTIVE,false).setValue(STATUS,0));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,UPPER,ACTIVE,STATUS);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){
  return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());
 }
 @Override public void setPlacedBy(Level w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){
  if(!w.isClientSide() && w.getBlockEntity(p) instanceof MachineBlockEntity be)be.restore(stack.getOrDefault(Machines.STATE,MachineState.EMPTY));
  EnergyNetwork.invalidate(w);
 }
 public static MachineBlockEntity machine(Level w,BlockPos p){
  var s=w.getBlockState(p);if(s.getBlock() instanceof MachineBlock && s.getValue(UPPER))p=p.below();
  return w.getBlockEntity(p) instanceof MachineBlockEntity be?be:null;
 }
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(UPPER)?null:new MachineBlockEntity(p,s);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> t){
  return !w.isClientSide() && !s.getValue(UPPER) && t==Machines.ENTITY?(world,pos,state,be)->EnergyNetwork.register((ServerLevel)world,(MachineBlockEntity)be):null;
 }
 @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){
  if(!w.isClientSide()){var be=machine(w,p);if(be!=null)player.openMenu(be);}
  return InteractionResult.SUCCESS;
 }
 @Override public boolean acceptsCable(BlockGetter w,BlockPos p,Direction side){
  var s=w.getBlockState(p);return !s.getValue(UPPER) && (side==input(s)||side==output(s));
 }
 public Direction input(BlockState s){return kind==MachineKind.GENERATOR?null:kind==MachineKind.CABINET?s.getValue(FACING).getCounterClockWise():s.getValue(FACING).getOpposite();}
 public Direction output(BlockState s){return (kind==MachineKind.FURNACE||kind==MachineKind.CRUSHER)?null:kind==MachineKind.CABINET?s.getValue(FACING).getClockWise():s.getValue(FACING).getOpposite();}
 @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder b){
  if(s.getValue(UPPER))return List.of();
  var be=b.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
  return List.of(be instanceof MachineBlockEntity m?m.dropStack():new ItemStack(Machines.ITEMS.get(kind)));
 }
 @Override public BlockState playerWillDestroy(Level w,BlockPos p,BlockState s,Player player){
  if(!w.isClientSide() && kind==MachineKind.CABINET && s.getValue(UPPER)){
   BlockPos lower=p.below();if(w.getBlockState(lower).is(this))w.destroyBlock(lower,!player.isCreative(),player);
  }
  return super.playerWillDestroy(w,p,s,player);
 }
 @Override protected void onPlace(BlockState s,Level w,BlockPos p,BlockState old,boolean moved){super.onPlace(s,w,p,old,moved);if(!old.is(this))EnergyNetwork.invalidate(w);}
}
