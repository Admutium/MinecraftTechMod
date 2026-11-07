package dev.admutium.techmod.machine;

import dev.admutium.techmod.TechMod;
import dev.admutium.techmod.cable.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import java.util.*;

/** A low rooftop panel and a separately owned floor cable share one cell. */
public final class SolarBlock extends MachineBlock {
 private static final VoxelShape BODY=Shapes.or(Block.box(0,4,0,16,8,16),Block.box(0,0,0,2,4,2),Block.box(14,0,0,16,4,2),Block.box(0,0,14,2,4,16),Block.box(14,0,14,16,4,16),Block.box(6,3.25,6,10,4,10));
 private static final Map<ServerLevel,Map<BlockPos,CableSnapshot>> DETACHED=new WeakHashMap<>();
 public SolarBlock(Properties p){super(MachineKind.SOLAR,p);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){
  var s=super.getStateForPlacement(c);var old=c.getLevel().getBlockState(c.getClickedPos());
  if(!canSurvive(s,c.getLevel(),c.getClickedPos())||!c.getLevel().getFluidState(c.getClickedPos()).isEmpty())return null;
  if(old.is(TechMod.CABLE)&&(!CableWorld.panelCompatible(CableWorld.data(c.getLevel(),c.getClickedPos()))||old.getValue(CableBlock.SNOW)>0))return null;
  return s;
 }
 @Override protected boolean canSurvive(BlockState s,LevelReader w,BlockPos p){return w.getBlockState(p.below()).isFaceSturdy(w,p.below(),Direction.UP);}
 @Override public Direction input(BlockState s){return null;}
 @Override public Direction output(BlockState s){return Direction.DOWN;}
 @Override public boolean acceptsCable(BlockGetter w,BlockPos p,Direction side){return false; /* output is the floor cable inside this cell */}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){
  return w.getBlockEntity(p) instanceof MachineBlockEntity be&&!be.snapshot().empty()?Shapes.joinUnoptimized(BODY,be.selectionShape(w),BooleanOp.OR):BODY;
 }
 @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return BODY;}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){
  if(player.isShiftKeyDown()&&player.getMainHandItem().isEmpty())return TechMod.WIRE_CUTTERS.useOn(new net.minecraft.world.item.context.UseOnContext(player,InteractionHand.MAIN_HAND,hit));
  return super.useWithoutItem(s,w,p,player,hit);
 }
 @Override protected void neighborChanged(BlockState s,Level w,BlockPos p,Block source,Orientation o,boolean moved){
  if(!w.isClientSide()&&!canSurvive(s,w,p))w.destroyBlock(p,true);
 }
 public static void detach(ServerLevel w,BlockPos p,CableSnapshot cable){if(!cable.empty())DETACHED.computeIfAbsent(w,k->new HashMap<>()).put(p.immutable(),cable);}
 @Override protected void affectNeighborsAfterRemoval(BlockState s,ServerLevel w,BlockPos p,boolean moved){super.affectNeighborsAfterRemoval(s,w,p,moved);restore(w,p);}
 private static void restore(ServerLevel w,BlockPos p){
  var pending=DETACHED.get(w);if(pending==null||!pending.containsKey(p)||CableWorld.isPanel(w.getBlockState(p)))return;
  var cable=pending.remove(p);
  if(w.getBlockState(p).isAir()&&CableWorld.supported(w,CableLayout.placement(p.getX(),p.getY(),p.getZ(),Face.DOWN))){var be=CableWorld.ensure(w,p);if(be!=null){be.update(cable);return;}}
  CableWorld.drop(w,p,TechMod.COPPER_CABLE,Long.bitCount(cable.wires()));
 }
 public static void restoreDetached(ServerLevel w){var pending=DETACHED.get(w);if(pending!=null)for(var p:List.copyOf(pending.keySet()))restore(w,p);}
}
