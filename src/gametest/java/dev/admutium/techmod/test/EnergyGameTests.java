package dev.admutium.techmod.test;
import dev.admutium.techmod.TechMod;
import dev.admutium.techmod.energy.*;
import dev.admutium.techmod.machine.*;
import dev.admutium.techmod.cable.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.nbt.NbtOps;
import java.util.*;
public final class EnergyGameTests {
 private MachineBlockEntity machine(GameTestHelper h,MachineKind kind,int x,int y,int z,Direction facing){
  BlockPos rel=new BlockPos(x,y,z);h.setBlock(rel,Machines.BLOCKS.get(kind).defaultBlockState().setValue(MachineBlock.FACING,facing));
  if(kind==MachineKind.CABINET)h.setBlock(rel.above(),Machines.BLOCKS.get(kind).defaultBlockState().setValue(MachineBlock.FACING,facing).setValue(MachineBlock.UPPER,true));
  var be=(MachineBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(rel));EnergyNetwork.register(h.getLevel(),be);return be;
 }
 private ItemStack battery(long energy){var s=new ItemStack(Machines.BATTERY);BatteryItem.setCharge(s,energy);return s;}
 @GameTest public void emptyDevicesAndBattery(GameTestHelper h){
  for(var kind:MachineKind.values()){var m=machine(h,kind,1+kind.ordinal()*2,1,1,Direction.NORTH);h.assertTrue(m.stored()==0,"New device is charged");if(kind==MachineKind.CABINET)h.assertTrue(m.capacity()==0,"Empty cabinet has capacity");}
  h.assertTrue(BatteryItem.charge(new ItemStack(Machines.BATTERY))==0,"New battery is charged");h.succeed();
 }
 @GameTest public void directGeneratorPowersEightSmeltsIn64Seconds(GameTestHelper h){
  var gen=machine(h,MachineKind.GENERATOR,1,1,1,Direction.WEST);var furnace=machine(h,MachineKind.FURNACE,2,1,1,Direction.EAST);
  gen.setItem(0,new ItemStack(Items.COAL,2));furnace.setItem(0,new ItemStack(Items.IRON_ORE,8));
  for(int i=0;i<1280;i++)EnergyNetwork.tick(h.getLevel());
  h.assertTrue(furnace.getItem(1).is(Items.IRON_INGOT)&&furnace.getItem(1).getCount()==8,"Eight smelts did not complete in 1280 ticks");h.assertTrue(gen.loss==0,"Adjacent blocks lose power");h.succeed();
 }
 @GameTest public void coalAndCharcoalYieldExactly100TE(GameTestHelper h){
  var gen=machine(h,MachineKind.GENERATOR,1,1,1,Direction.WEST);gen.setItem(0,new ItemStack(Items.CHARCOAL));
  for(int i=0;i<900;i++){gen.beginTick();gen.finishTick();}
  h.assertTrue(gen.stored()==Energy.BUFFER&&gen.fuelTicks==0,"Fuel yield or duration incorrect");
  gen.setItem(0,new ItemStack(Items.COAL));gen.beginTick();h.assertTrue(gen.getItem(0).getCount()==1,"Full generator consumed fuel");h.succeed();
 }
 @GameTest public void wrongPortCannotReceiveEnergy(GameTestHelper h){
  var gen=machine(h,MachineKind.GENERATOR,1,1,1,Direction.WEST);var furnace=machine(h,MachineKind.FURNACE,2,1,1,Direction.NORTH);
  gen.setItem(0,new ItemStack(Items.COAL));for(int i=0;i<40;i++)EnergyNetwork.tick(h.getLevel());h.assertTrue(furnace.stored()==0,"Energy entered non-input side");h.succeed();
 }
 @GameTest public void cablePathConservesEnergyIncludingLosses(GameTestHelper h){
  var gen=machine(h,MachineKind.GENERATOR,1,1,1,Direction.WEST);var furnace=machine(h,MachineKind.FURNACE,5,1,1,Direction.EAST);
  for(int x=2;x<5;x++){
   h.setBlock(new BlockPos(x,0,1),Blocks.STONE);BlockPos p=h.absolutePos(new BlockPos(x,1,1));
   var be=CableWorld.ensure(h.getLevel(),p);be.update(new CableSnapshot(CableLayout.placement(p.getX(),p.getY(),p.getZ(),Face.DOWN).bit(),0,List.of()));
  }
  gen.setItem(0,new ItemStack(Items.COAL,2));furnace.setItem(0,new ItemStack(Items.IRON_ORE,8));long produced=0,used=0,lost=0;
  for(int i=0;i<1000;i++){EnergyNetwork.tick(h.getLevel());if(gen.working)produced+=Energy.GENERATION;if(furnace.working)used+=Energy.SMELT_TICK;lost+=gen.loss;}
  h.assertTrue(lost>0,"Cable has no losses");h.assertTrue(produced==used+lost+gen.stored()+furnace.stored(),"Energy was duplicated or silently destroyed");h.succeed();
 }
 @GameTest public void cabinetUsesEightIndependentBatteries(GameTestHelper h){
  var cabinet=machine(h,MachineKind.CABINET,2,1,2,Direction.NORTH);for(int i=0;i<8;i++)cabinet.setItem(i,battery(i*Energy.UNIT));
  h.assertTrue(cabinet.capacity()==2400*Energy.UNIT,"Cabinet capacity incorrect");long before=cabinet.stored();long received=cabinet.receive(137*Energy.UNIT);
  h.assertTrue(cabinet.stored()==before+received,"Charging duplicated energy");long taken=cabinet.extract(35*Energy.UNIT);h.assertTrue(cabinet.stored()==before+received-taken,"Discharge lost energy");h.succeed();
 }
 @GameTest public void portableStateRoundTripPreservesFuelAndBatteries(GameTestHelper h){
  var gen=machine(h,MachineKind.GENERATOR,1,1,1,Direction.NORTH);gen.setItem(0,new ItemStack(Items.COAL));gen.setItem(1,battery(123*Energy.UNIT));gen.beginTick();gen.finishTick();
  var stack=gen.dropStack();var ops=h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
  var encoded=ItemStack.CODEC.encodeStart(ops,stack).getOrThrow();var decoded=ItemStack.CODEC.parse(ops,encoded).getOrThrow();
  var restored=machine(h,MachineKind.GENERATOR,4,1,1,Direction.NORTH);restored.restore(decoded.get(Machines.STATE));
  h.assertTrue(restored.energy==gen.energy&&restored.fuelTicks==gen.fuelTicks,"Stored generator fuel or charge changed");
  h.assertTrue(BatteryItem.charge(restored.getItem(1))==BatteryItem.charge(gen.getItem(1)),"Battery charge did not round-trip");h.succeed();
 }
 @GameTest public void furnaceWorksFromRemovableBattery(GameTestHelper h){
  var furnace=machine(h,MachineKind.FURNACE,2,1,2,Direction.NORTH);furnace.setItem(0,new ItemStack(Items.SAND));furnace.setItem(2,battery(300*Energy.UNIT));
  for(int i=0;i<160;i++){furnace.beginTick();furnace.finishTick();}
  h.assertTrue(furnace.getItem(1).is(Items.GLASS),"Battery did not power furnace");h.assertTrue(BatteryItem.charge(furnace.getItem(2))==284*Energy.UNIT,"Battery used wrong energy");h.succeed();
 }
 @GameTest public void generatorChargesRemovableBattery(GameTestHelper h){
  var gen=machine(h,MachineKind.GENERATOR,2,1,2,Direction.NORTH);gen.setItem(0,new ItemStack(Items.COAL));gen.setItem(1,battery(0));
  for(int i=0;i<900;i++){gen.beginTick();gen.finishTick();}
  h.assertTrue(BatteryItem.charge(gen.getItem(1))==100*Energy.UNIT&&gen.energy==0,"Charging yield incorrect");h.succeed();
 }
 @GameTest public void breakingCabinetDropsOnePackedCabinet(GameTestHelper h){
  var cabinet=machine(h,MachineKind.CABINET,2,1,2,Direction.NORTH);for(int i=0;i<8;i++)cabinet.setItem(i,battery((i+1)*Energy.UNIT));
  BlockPos p=cabinet.getBlockPos();h.getLevel().destroyBlock(p,true);
  var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(p).inflate(3));
  long total=0;int cabinets=0;for(var drop:drops){var s=drop.getItem();h.assertTrue(!s.is(Machines.BATTERY),"Battery dropped twice");if(s.is(Machines.ITEMS.get(MachineKind.CABINET))){cabinets++;for(var b:s.get(Machines.STATE).batteries())total+=BatteryItem.charge(b);}}
  h.assertTrue(cabinets==1&&total==36*Energy.UNIT,"Packed cabinet lost contents or dropped twice");h.assertTrue(h.getLevel().getBlockState(p.above()).isAir(),"Upper cabinet was orphaned");h.succeed();
 }

 @GameTest public void cabinetTransfersThroughSeparateInputAndOutput(GameTestHelper h){
  var gen=machine(h,MachineKind.GENERATOR,1,1,2,Direction.WEST);var cabinet=machine(h,MachineKind.CABINET,2,1,2,Direction.NORTH);var furnace=machine(h,MachineKind.FURNACE,3,1,2,Direction.EAST);
  cabinet.setItem(0,battery(0));gen.setItem(0,new ItemStack(Items.COAL,2));furnace.setItem(0,new ItemStack(Items.IRON_ORE,8));
  for(int i=0;i<1280;i++)EnergyNetwork.tick(h.getLevel());h.assertTrue(furnace.getItem(1).getCount()==8,"Cabinet did not pass energy from input to output");h.succeed();
 }
 @GameTest public void cabinetUpperHalfBreakPreservesOneInventory(GameTestHelper h){
  var cabinet=machine(h,MachineKind.CABINET,2,1,2,Direction.NORTH);cabinet.setItem(0,battery(250*Energy.UNIT));BlockPos p=cabinet.getBlockPos();h.getLevel().destroyBlock(p.above(),true);
  var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(p).inflate(3));int count=0;long charge=0;
  for(var e:drops)if(e.getItem().is(Machines.ITEMS.get(MachineKind.CABINET))){count++;for(var b:e.getItem().get(Machines.STATE).batteries())charge+=BatteryItem.charge(b);}
  h.assertTrue(count==1&&charge==250*Energy.UNIT,"Upper half lost or duplicated battery inventory");h.succeed();
 }
 @GameTest public void furnaceBreakSeparatesMaterialsButKeepsBattery(GameTestHelper h){
  var f=machine(h,MachineKind.FURNACE,2,1,2,Direction.NORTH);f.setItem(0,new ItemStack(Items.IRON_ORE,3));f.setItem(1,new ItemStack(Items.IRON_INGOT,2));f.setItem(2,battery(170*Energy.UNIT));f.energy=30*Energy.UNIT;f.progress=70;
  var p=f.getBlockPos();h.getLevel().destroyBlock(p,true);int ores=0,ingots=0,machines=0;
  for(var e:h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(p).inflate(3))){var item=e.getItem();if(item.is(Items.IRON_ORE))ores+=item.getCount();if(item.is(Items.IRON_INGOT))ingots+=item.getCount();if(item.is(Machines.ITEMS.get(MachineKind.FURNACE))){machines++;var st=item.get(Machines.STATE);h.assertTrue(st.energy()==30*Energy.UNIT&&BatteryItem.charge(st.batteries().getFirst())==170*Energy.UNIT,"Machine charge was lost");}h.assertTrue(!item.is(Machines.BATTERY),"Battery spilled outside machine");}
  h.assertTrue(ores==3&&ingots==2&&machines==1,"Materials or machine dropped incorrectly");h.succeed();
 }
 @GameTest public void removingCableInvalidatesCachedRoutes(GameTestHelper h){
  var gen=machine(h,MachineKind.GENERATOR,1,1,1,Direction.WEST);var furnace=machine(h,MachineKind.FURNACE,4,1,1,Direction.EAST);
  for(int x=2;x<=3;x++){h.setBlock(new BlockPos(x,0,1),Blocks.STONE);var p=h.absolutePos(new BlockPos(x,1,1));CableWorld.ensure(h.getLevel(),p).update(new CableSnapshot(CableLayout.placement(p.getX(),p.getY(),p.getZ(),Face.DOWN).bit(),0,List.of()));}
  gen.setItem(0,new ItemStack(Items.COAL));EnergyNetwork.tick(h.getLevel());h.assertTrue(furnace.incoming>0,"Initial line is not connected");
  h.getLevel().destroyBlock(h.absolutePos(new BlockPos(2,1,1)),false);furnace.energy=0;EnergyNetwork.tick(h.getLevel());h.assertTrue(furnace.incoming==0,"Removed cable left a ghost route");h.succeed();
 }

 @GameTest public void suspensionCostsLengthAndRefundsOnlyOnce(GameTestHelper h){
  Node a=holder(h,2,3,2,Face.NORTH),b=holder(h,5,3,2,Face.NORTH);var span=new CableSpan(UUID.randomUUID(),a,b);var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
  var insufficient=new ItemStack(TechMod.COPPER_CABLE,2);String error=CableWorld.createSpan(h.getLevel(),player,span,insufficient);
  h.assertTrue("not_enough_wire".equals(error)&&insufficient.getCount()==2&&CableWorld.data(h.getLevel(),CableWorld.pos(a)).spans().isEmpty(),"Insufficient inventory mutated the world");
  var wires=new ItemStack(TechMod.COPPER_CABLE,4);h.assertTrue(CableWorld.createSpan(h.getLevel(),player,span,wires)==null&&wires.getCount()==1,"Wrong span cost");
  CableWorld.removeSpan(h.getLevel(),span,true);CableWorld.removeSpan(h.getLevel(),span,true);int count=0;
  for(var e:h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(CableWorld.pos(a)).inflate(3)))if(e.getItem().is(TechMod.COPPER_CABLE))count+=e.getItem().getCount();
  h.assertTrue(count==3,"Span refund duplicated or lost items");h.succeed();
 }
 private Node holder(GameTestHelper h,int x,int y,int z,Face face){
  var p=h.absolutePos(new BlockPos(x,y,z));h.getLevel().setBlock(p.relative(CableWorld.direction(face)),Blocks.STONE.defaultBlockState(),3);
  Node n=CableLayout.placement(p.getX(),p.getY(),p.getZ(),face);CableWorld.ensure(h.getLevel(),p).update(new CableSnapshot(0,1<<face.ordinal(),List.of()));return n;
 }
 @GameTest public void suspendedCableTransfersEnergy(GameTestHelper h){
  var gen=machine(h,MachineKind.GENERATOR,1,3,2,Direction.WEST);var furnace=machine(h,MachineKind.FURNACE,6,3,2,Direction.EAST);
  var pa=h.absolutePos(new BlockPos(2,3,2));var pb=h.absolutePos(new BlockPos(5,3,2));
  Node a=CableLayout.placement(pa.getX(),pa.getY(),pa.getZ(),Face.WEST),b=CableLayout.placement(pb.getX(),pb.getY(),pb.getZ(),Face.EAST);
  CableWorld.ensure(h.getLevel(),pa).update(new CableSnapshot(0,1<<Face.WEST.ordinal(),List.of()));CableWorld.ensure(h.getLevel(),pb).update(new CableSnapshot(0,1<<Face.EAST.ordinal(),List.of()));
  var span=new CableSpan(UUID.randomUUID(),a,b);var wires=new ItemStack(TechMod.COPPER_CABLE,5);
  h.assertTrue(CableWorld.createSpan(h.getLevel(),h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL),span,wires)==null,"Hanging cable could not be created");
  gen.setItem(0,new ItemStack(Items.COAL));furnace.setItem(0,new ItemStack(Items.IRON_ORE));for(int i=0;i<160;i++)EnergyNetwork.tick(h.getLevel());
  h.assertTrue(furnace.getItem(1).is(Items.IRON_INGOT)&&gen.loss>0,"Hanging energy route failed");h.succeed();
 }

 @GameTest public void brownoutDoesNotFlickerVisualStateEveryTick(GameTestHelper h){
  var f=machine(h,MachineKind.FURNACE,2,1,2,Direction.NORTH);f.setItem(0,new ItemStack(Items.IRON_ORE,8));int rawFlips=0,visualFlips=0;boolean raw=false,visual=false;
  for(int i=0;i<100;i++){f.beginTick();f.energy+=900;f.finishTick();if(raw!=f.working)rawFlips++;if(visual!=f.visualActive)visualFlips++;raw=f.working;visual=f.visualActive;}
  h.assertTrue(rawFlips>80&&visualFlips<=2,"Brownout causes per-tick visual rebuilds");h.assertTrue(f.indicator==2,"Brownout must show yellow power warning");h.assertTrue(f.energy==0,"Brownout changed energy accounting");h.succeed();
 }
}
