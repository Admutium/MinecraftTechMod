package dev.admutium.techmod.test;
import dev.admutium.techmod.TechMod;
import dev.admutium.techmod.cable.*;
import dev.admutium.techmod.energy.*;
import dev.admutium.techmod.machine.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;
public final class SolarGameTests {
 private MachineBlockEntity panel(GameTestHelper h,BlockPos r){h.setBlock(r.below(),Blocks.STONE);h.setBlock(r,Machines.BLOCKS.get(MachineKind.SOLAR));var be=h.getBlockEntity(r,MachineBlockEntity.class);var w=h.getLevel();var p=be.getBlockPos();int roof=w.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,p.getX(),p.getZ());for(int y=p.getY()+1;y<roof;y++){var at=new BlockPos(p.getX(),y,p.getZ());if(w.getBlockState(at).is(Blocks.BARRIER))w.removeBlock(at,false);}EnergyNetwork.register(h.getLevel(),be);return be;}
 private String debug(GameTestHelper h,MachineBlockEntity be){
  var w=h.getLevel();StringBuilder s=new StringBuilder(" [time="+w.getOverworldClockTime()+", sun="+SolarPower.hasSun(w)+", eff="+be.solarEfficiency+", status="+be.solarStatus+", energy="+be.energy+", above=");
  var p=be.getBlockPos();for(int y=p.getY()+1;y<w.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,p.getX(),p.getZ());y++){var at=new BlockPos(p.getX(),y,p.getZ());var st=w.getBlockState(at);if(!st.isAir())s.append(y).append(":").append(st).append(" light=").append(st.getLightDampening()).append(";");}return s+"]";
 }
 private void noon(GameTestHelper h){h.setTime(6000);h.getLevel().setRainLevel(0);h.getLevel().setThunderLevel(0);}
 private CableSnapshot floor(BlockPos p){return new CableSnapshot(CableLayout.placement(p.getX(),p.getY(),p.getZ(),Face.DOWN).bit(),0,List.of());}
 @GameTest public void clearNoonProducesHalfTEPerSecondAndClampsBuffer(GameTestHelper h){
  noon(h);var be=panel(h,new BlockPos(2,1,2));for(int i=0;i<20;i++){be.beginTick();be.finishTick();}
  h.assertTrue(be.energy==Energy.UNIT/2&&be.solarEfficiency==10000,"Noon yield or efficiency incorrect"+debug(h,be));
  for(int i=0;i<500;i++){be.beginTick();be.finishTick();}h.assertTrue(be.capacity()==10*Energy.UNIT&&be.energy==be.capacity()&&be.solarRate==0,"Solar buffer overfilled");h.succeed();
 }
 @GameTest public void nightAndShadingPreventGeneration(GameTestHelper h){
  h.setTime(18000);var be=panel(h,new BlockPos(2,1,2));be.beginTick();h.assertTrue(be.energy==0&&be.solarStatus==3,"Night produced energy"+debug(h,be));
  noon(h);h.setBlock(new BlockPos(4,3,2),Blocks.STONE);var shaded=panel(h,new BlockPos(4,1,2));shaded.beginTick();h.assertTrue(shaded.energy==0&&shaded.solarStatus==2,"Opaque roof did not shade panel");h.succeed();
 }
 @GameTest public void glassPassesSunButLeavesWaterAndSlabsBlockIt(GameTestHelper h){
  var be=panel(h,new BlockPos(2,1,2));var above=new BlockPos(2,3,2);
  for(var b:new net.minecraft.world.level.block.Block[]{Blocks.GLASS,Blocks.STAINED_GLASS.blue()}){h.setBlock(above,b);h.assertTrue(SolarPower.skyOpen(h.getLevel(),be.getBlockPos()),"Transparent glass blocked sun"+debug(h,be));}
  for(var b:new net.minecraft.world.level.block.Block[]{Blocks.OAK_LEAVES,Blocks.TINTED_GLASS,Blocks.WATER,Blocks.STONE_SLAB}){h.setBlock(above,b);h.assertTrue(!SolarPower.skyOpen(h.getLevel(),be.getBlockPos()),"Opaque shade or water passed sun");}h.succeed();
 }
 @GameTest public void weatherReducesPowerOnlyInWetBiomes(GameTestHelper h){
  var be=panel(h,new BlockPos(2,1,2));var w=h.getLevel();boolean wet=w.getBiome(be.getBlockPos()).value().hasPrecipitation();
  w.setRainLevel(1);w.setThunderLevel(0);h.assertTrue(Math.abs(SolarPower.weather(w,be.getBlockPos())-(wet?.4:1))<.00001,"Rain coefficient incorrect");
  w.setThunderLevel(1);h.assertTrue(Math.abs(SolarPower.weather(w,be.getBlockPos())-(wet?.1:1))<.00001,"Thunder coefficient incorrect");w.setRainLevel(0);w.setThunderLevel(0);h.succeed();
 }
 @GameTest public void placementOverCableAndPanelRemovalKeepSeparateOwnership(GameTestHelper h){
  var r=new BlockPos(2,1,2);h.setBlock(r.below(),Blocks.STONE);var w=h.getLevel();var p=h.absolutePos(r);CableWorld.ensure(w,p).update(floor(p));
  var player=h.makeMockPlayer(GameType.SURVIVAL);var item=new ItemStack(Machines.ITEMS.get(MachineKind.SOLAR));player.setItemInHand(InteractionHand.MAIN_HAND,item);
  var hit=new BlockHitResult(Vec3.atCenterOf(p),Direction.UP,p,false);var context=new BlockPlaceContext(new UseOnContext(player,InteractionHand.MAIN_HAND,hit));
  var result=((BlockItem)item.getItem()).place(context);h.assertTrue(result.consumesAction()&&CableWorld.isPanel(w.getBlockState(p))&&CableWorld.data(w,p).wires()==floor(p).wires(),"Placing panel erased or rejected floor cable");
  w.destroyBlock(p,true);SolarBlock.restoreDetached(w);h.assertTrue(w.getBlockState(p).is(TechMod.CABLE)&&CableWorld.data(w,p).wires()==floor(p).wires(),"Breaking panel erased its independently owned cable");
  int panelDrops=0,wireDrops=0;for(var drop:w.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(p).inflate(2))){if(drop.getItem().is(Machines.ITEMS.get(MachineKind.SOLAR)))panelDrops+=drop.getItem().getCount();if(drop.getItem().is(TechMod.COPPER_CABLE))wireDrops+=drop.getItem().getCount();}
  h.assertTrue(panelDrops==1&&wireDrops==0,"Panel removal duplicated wire or panel items");h.succeed();
 }
 @GameTest public void wireCanBeInstalledAndRemovedWithoutRemovingPanel(GameTestHelper h){
  var be=panel(h,new BlockPos(2,1,2));var p=be.getBlockPos();var w=h.getLevel();var player=h.makeMockPlayer(GameType.SURVIVAL);
  var wire=new ItemStack(TechMod.COPPER_CABLE,2);player.setItemInHand(InteractionHand.MAIN_HAND,wire);var hit=new BlockHitResult(Vec3.atCenterOf(p),Direction.UP,p,false);
  var context=new UseOnContext(player,InteractionHand.MAIN_HAND,hit);TechMod.COPPER_CABLE.useOn(context);TechMod.COPPER_CABLE.useOn(context);
  h.assertTrue(wire.getCount()==1&&be.snapshot().wires()==floor(p).wires(),"Panel wire consumed wrong amount or landed on wrong face");
  player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);TechMod.WIRE_CUTTERS.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit));
  h.assertTrue(CableWorld.isPanel(w.getBlockState(p))&&be.snapshot().empty(),"Removing wire removed panel");h.succeed();
 }
 @GameTest public void panelRequiresWireAndAdjacentPanelsDoNotTransferDirectly(GameTestHelper h){
  noon(h);var be=panel(h,new BlockPos(2,1,2));var other=panel(h,new BlockPos(3,1,2));be.energy=Energy.UNIT*5;
  var furnacePos=new BlockPos(2,0,2);h.setBlock(furnacePos,Machines.BLOCKS.get(MachineKind.FURNACE));var f=h.getBlockEntity(furnacePos,MachineBlockEntity.class);EnergyNetwork.register(h.getLevel(),f);
  for(int i=0;i<10;i++)EnergyNetwork.tick(h.getLevel());h.assertTrue(f.energy==0&&other.incoming==0,"Unwired solar panel powered a neighbor directly");h.succeed();
 }
 @GameTest public void solarFloorWireFeedsCabinetAndConservesEnergy(GameTestHelper h){
  noon(h);var be=panel(h,new BlockPos(2,1,2));var w=h.getLevel();be.update(floor(be.getBlockPos()));
  h.setBlock(new BlockPos(3,0,2),Blocks.STONE);var p=h.absolutePos(new BlockPos(3,1,2));CableWorld.ensure(w,p).update(floor(p));
  h.setBlock(new BlockPos(4,1,2),Machines.BLOCKS.get(MachineKind.CABINET).defaultBlockState().setValue(MachineBlock.FACING,Direction.NORTH));var cabinet=h.getBlockEntity(new BlockPos(4,1,2),MachineBlockEntity.class);cabinet.setItem(0,new ItemStack(Machines.BATTERY));EnergyNetwork.register(w,cabinet);
  long generated=0,lost=0;for(int i=0;i<200;i++){EnergyNetwork.tick(w);generated+=be.solarRate/20;lost+=be.loss;}
  h.assertTrue(cabinet.stored()>0&&be.outgoing>0,"Solar cable route failed"+debug(h,be));h.assertTrue(generated==be.energy+cabinet.stored()+lost,"Solar network lost or duplicated energy");h.succeed();
 }
 @GameTest public void portablePanelHasNoBatterySlotsAndClampsItsOwnCapacity(GameTestHelper h){
  var be=panel(h,new BlockPos(2,1,2));be.energy=7*Energy.UNIT;var saved=be.portable();var other=panel(h,new BlockPos(4,1,2));other.restore(saved);
  h.assertTrue(other.energy==be.energy&&other.getContainerSize()==0&&saved.batteries().isEmpty(),"Portable panel added a battery or lost charge");
  other.restore(new MachineState(300*Energy.UNIT,0,List.of(new ItemStack(Machines.BATTERY)),0));h.assertTrue(other.energy==10*Energy.UNIT&&other.getContainerSize()==0,"Panel accepted legacy machine capacity or battery");h.succeed();
 }
 @GameTest public void worldSaveKeepsPanelChargeAndEmbeddedFloorWire(GameTestHelper h){
  var be=panel(h,new BlockPos(2,1,2));be.energy=7*Energy.UNIT;be.update(floor(be.getBlockPos()));
  var tag=be.saveWithFullMetadata(h.getLevel().registryAccess());
  var restored=(MachineBlockEntity)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),tag,h.getLevel().registryAccess());
  h.assertTrue(restored!=null&&restored.energy==be.energy&&restored.snapshot().equals(be.snapshot())&&restored.getContainerSize()==0,"World save lost solar cable, charge or zero-slot layout");h.succeed();
 }

}
