package dev.admutium.techmod.conveyor;
import dev.admutium.techmod.machine.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
/** Cached mechanical coverage. Cargo offers are applied only after movement snapshots. No item entities. */
public final class ConveyorNetwork {
 private static final Map<ServerLevel,Network> WORLDS=new HashMap<>();
 private static final class Network {
  final Map<BlockPos,ConveyorEntity> parts=new HashMap<>();final Map<BlockPos,MachineBlockEntity> drives=new HashMap<>();final Map<BlockPos,MachineBlockEntity> owners=new HashMap<>();boolean dirty=true;long tick;
 }
 public static void clearAll(){WORLDS.clear();}public static void clear(ServerLevel w){WORLDS.remove(w);}
 public static void invalidate(Level w){if(w instanceof ServerLevel s){var n=WORLDS.get(s);if(n!=null)n.dirty=true;}}
 public static void register(ServerLevel w,ConveyorEntity be){var n=WORLDS.computeIfAbsent(w,k->new Network());if(n.parts.put(be.getBlockPos(),be)!=be)n.dirty=true;}
 public static void registerDrive(ServerLevel w,MachineBlockEntity be){var n=WORLDS.computeIfAbsent(w,k->new Network());if(n.drives.put(be.getBlockPos(),be)!=be)n.dirty=true;}
 private static void connect(Map<BlockPos,Set<BlockPos>> edges,BlockPos a,BlockPos b){if(!edges.containsKey(b))return;edges.get(a).add(b);edges.get(b).add(a);}
 private static void rebuild(Network n){
  var edges=new HashMap<BlockPos,Set<BlockPos>>();for(var p:n.parts.keySet())edges.put(p,new HashSet<>());
  for(var be:n.parts.values()){
   var p=be.getBlockPos();var s=be.getBlockState();var d=s.getValue(ConveyorBlock.FACING);
   if(be.kind()==Conveyors.Kind.MANIPULATOR){connect(edges,p,p.relative(d));connect(edges,p,p.relative(d.getOpposite()));}
   else if(be.kind()==Conveyors.Kind.SPLITTER){connect(edges,p,p.relative(d.getClockWise()));connect(edges,p,p.relative(d.getCounterClockWise()));}
   else connect(edges,p,ConveyorBlock.output(p,s));
  }
  var ranges=new HashMap<MachineBlockEntity,Map<BlockPos,Integer>>();var drivers=new ArrayList<>(n.drives.values());drivers.sort(Comparator.comparingLong(b->b.getBlockPos().asLong()));
  for(var drive:drivers){drive.driveLoad=0;var start=drive.getBlockPos().relative(drive.getBlockState().getValue(MachineBlock.FACING));var section=n.parts.get(start);var distances=new HashMap<BlockPos,Integer>();ranges.put(drive,distances);
   if(section==null||section.kind()!=Conveyors.Kind.BELT||ConveyorBlock.slope(section.getBlockState())!=0||ConveyorBlock.turn(section.getBlockState())!=0||section.getBlockState().getValue(ConveyorBlock.FACING).getAxis()==drive.getBlockState().getValue(MachineBlock.FACING).getAxis())continue;
   var queue=new ArrayDeque<BlockPos>();queue.add(start);distances.put(start,0);
   while(!queue.isEmpty()&&distances.size()<4096){var at=queue.remove();int distance=distances.get(at);if(distance>=15)continue;for(var next:edges.getOrDefault(at,Set.of()))if(!distances.containsKey(next)){distances.put(next,distance+1);queue.add(next);}}
  }
  n.owners.entrySet().removeIf(e->!n.parts.containsKey(e.getKey())||!ranges.getOrDefault(e.getValue(),Map.of()).containsKey(e.getKey()));
  for(var driver:drivers){var previous=n.owners.entrySet().stream().filter(e->e.getValue()==driver).sorted(Map.Entry.comparingByKey(Comparator.comparingLong(BlockPos::asLong))).toList();for(var e:previous)if(driver.driveLoad++>=16){n.owners.remove(e.getKey());driver.driveLoad--;}}
  var positions=new ArrayList<>(n.parts.keySet());positions.sort(Comparator.comparingLong(BlockPos::asLong));
  for(var p:positions)if(!n.owners.containsKey(p)){
   MachineBlockEntity best=null;int distance=100;for(var driver:drivers){int d=ranges.get(driver).getOrDefault(p,100);if(driver.driveLoad<16&&d<distance){distance=d;best=driver;}}
   if(best!=null){n.owners.put(p,best);best.driveLoad++;}
  }for(var e:n.owners.entrySet()){var be=n.parts.get(e.getKey());int distance=ranges.get(e.getValue()).getOrDefault(e.getKey(),0);if(be.driveDistance!=distance){be.driveDistance=distance;be.sync();}}n.dirty=false;
 }
 private static ConveyorEntity target(ServerLevel w,Network n,ConveyorEntity from,BlockPos p){
  if(!w.hasChunkAt(p))return null;var be=n.parts.get(p);if(be==null||be.kind()==Conveyors.Kind.MANIPULATOR)return null;
  Direction side=null;for(var d:Direction.Plane.HORIZONTAL)if(from.getBlockPos().getX()-p.getX()==d.getStepX()&&from.getBlockPos().getZ()-p.getZ()==d.getStepZ())side=d;
  return side==ConveyorBlock.input(be.getBlockState())?be:null;
 }
 private static boolean canExit(ServerLevel w,Network n,ConveyorEntity be,long tick){
  if(be.cargo.isEmpty())return false;var s=be.getBlockState();var d=s.getValue(ConveyorBlock.FACING);var cargo=be.cargo.getFirst().stack;
  if(be.kind()==Conveyors.Kind.SPLITTER){var a=target(w,n,be,be.getBlockPos().relative(d.getCounterClockWise()));var b=target(w,n,be,be.getBlockPos().relative(d.getClockWise()));int count=cargo.getCount();if(count==1){var dest=be.rightNext?b:a;return dest!=null&&dest.powered&&dest.room(cargo,tick);}return a!=null&&b!=null&&a.powered&&b.powered&&a.room(cargo,tick)&&b.room(cargo,tick);}
  var dest=target(w,n,be,ConveyorBlock.output(be.getBlockPos(),s));return dest!=null&&dest.powered&&dest.room(cargo,tick);
 }
 private static void exit(ServerLevel w,Network n,ConveyorEntity be,long tick){
  if(!canExit(w,n,be,tick))return;var s=be.getBlockState();var d=s.getValue(ConveyorBlock.FACING);var cargo=be.cargo.getFirst().stack;
  if(be.kind()==Conveyors.Kind.SPLITTER){int left=cargo.getCount()/2,right=left;if((cargo.getCount()&1)!=0){if(be.rightNext)right++;else left++;be.rightNext=!be.rightNext;}
   if(left>0)target(w,n,be,be.getBlockPos().relative(d.getCounterClockWise())).offer(cargo.copyWithCount(left),tick);
   if(right>0)target(w,n,be,be.getBlockPos().relative(d.getClockWise())).offer(cargo.copyWithCount(right),tick);be.lastGate=tick;
  }else target(w,n,be,ConveyorBlock.output(be.getBlockPos(),s)).offer(cargo,tick);
  be.cargo.removeFirst();be.sync();
 }
 private static BlockPos source(ConveyorEntity be){var d=be.getBlockState().getValue(ConveyorBlock.FACING);return be.getBlockPos().relative(be.reverse?d:d.getOpposite());}
 private static BlockPos sink(ConveyorEntity be){var d=be.getBlockState().getValue(ConveyorBlock.FACING);return be.getBlockPos().relative(be.reverse?d.getOpposite():d);}
 private static Direction face(ConveyorEntity be,boolean input){var d=be.getBlockState().getValue(ConveyorBlock.FACING);return input?(be.reverse?d.getOpposite():d):(be.reverse?d:d.getOpposite());}
 private static boolean armCanWork(ServerLevel w,ConveyorEntity be,long tick){
  if(be.armPhase>10)return true;if(!be.hand.isEmpty())return ItemAccess.canPut(w,sink(be),face(be,false),be.hand,tick);
  var candidate=ItemAccess.peek(w,source(be),face(be,true),be);return !candidate.isEmpty()&&ItemAccess.canPut(w,sink(be),face(be,false),candidate,tick);
 }
 private static void arm(ServerLevel w,ConveyorEntity be,long tick){
  if(be.armPhase==0){var candidate=ItemAccess.peek(w,source(be),face(be,true),be);if(candidate.isEmpty()||!ItemAccess.canPut(w,sink(be),face(be,false),candidate,tick))return;be.hand=ItemAccess.take(w,source(be),face(be,true),be);if(be.hand.isEmpty())return;be.armPhase=1;be.sync();}
  else if(be.armPhase<10){be.armPhase++;be.setChanged();}
  else if(be.armPhase==10){if(ItemAccess.put(w,sink(be),face(be,false),be.hand,tick)){be.hand=net.minecraft.world.item.ItemStack.EMPTY;be.armPhase=11;be.sync();}}
  else {be.armPhase++;if(be.armPhase>=20){be.armPhase=0;be.sync();}else be.setChanged();}
 }
 public static void tick(ServerLevel w){
  var n=WORLDS.get(w);if(n==null)return;long tick=w.getGameTime();n.tick++;
  if(n.parts.entrySet().removeIf(e->e.getValue().isRemoved()||!w.hasChunkAt(e.getKey())||w.getBlockEntity(e.getKey())!=e.getValue()))n.dirty=true;
  if(n.drives.entrySet().removeIf(e->e.getValue().isRemoved()||!w.hasChunkAt(e.getKey())||w.getBlockEntity(e.getKey())!=e.getValue()))n.dirty=true;
  if(n.dirty)rebuild(n);
  var previousPower=new HashMap<BlockPos,Boolean>();for(var be:n.parts.values())previousPower.put(be.getBlockPos(),be.powered);
  // Provisional power lets capacity checks inspect destinations; actual power is assigned below.
  for(var be:n.parts.values())be.powered=n.owners.containsKey(be.getBlockPos());
  var parts=new ArrayList<>(n.parts.values());parts.sort(Comparator.comparingLong(be->be.getBlockPos().asLong()));
  var busy=new HashSet<MachineBlockEntity>();var occupied=new HashSet<MachineBlockEntity>();
  for(var be:parts){var driver=n.owners.get(be.getBlockPos());if(driver==null)continue;
   if(!be.cargo.isEmpty()||!be.hand.isEmpty())occupied.add(driver);
   if(be.kind()==Conveyors.Kind.MANIPULATOR?armCanWork(w,be,tick):!be.cargo.isEmpty()&&(be.cargo.getFirst().progress<20||canExit(w,n,be,tick)))busy.add(driver);
  }
  var allowed=new HashMap<MachineBlockEntity,Integer>();
  for(var driver:n.drives.values()){
   boolean signal=w.hasNeighborSignal(driver.getBlockPos());boolean enabled=driver.driveMode==0||driver.driveMode==1&&signal||driver.driveMode==2&&!signal;
   long cost=driver.driveLoad==0?0:busy.contains(driver)?18L+driver.driveLoad*(n.tick%2==0?4:5):occupied.contains(driver)?0:18;
   boolean power=enabled&&driver.driveLoad>0&&driver.energy>=Math.max(1,cost);if(power){driver.energy-=cost;driver.setChanged();}
   int status=!enabled?3:power?0:2;allowed.put(driver,status);driver.working=power;driver.visualActive=power;
   boolean visual=power&&(busy.contains(driver)||!occupied.contains(driver));var s=driver.getBlockState();if(tick%10==0&&(s.getValue(MachineBlock.ACTIVE)!=visual||s.getValue(MachineBlock.STATUS)!=(power?1:driver.energy>0?2:0)))w.setBlock(driver.getBlockPos(),s.setValue(MachineBlock.ACTIVE,visual).setValue(MachineBlock.STATUS,power?1:driver.energy>0?2:0),2);
  }
  var exiting=new ArrayList<ConveyorEntity>();
  for(var be:parts){boolean old=previousPower.getOrDefault(be.getBlockPos(),false),oldMoving=be.moving;int oldStatus=be.status;var driver=n.owners.get(be.getBlockPos());var oldDrive=be.drivePos;be.drivePos=driver==null?null:driver.getBlockPos();be.status=driver==null?1:allowed.getOrDefault(driver,2);be.powered=be.status==0;be.moving=false;
   if(be.powered){if(be.kind()==Conveyors.Kind.MANIPULATOR){arm(w,be,tick);be.moving=be.armPhase>0;}else{
    for(int i=0;i<be.cargo.size();i++){var cargo=be.cargo.get(i);int max=i==0?20:Math.max(0,be.cargo.get(i-1).progress-10);if(cargo.progress<max){cargo.progress++;be.moving=true;be.setChanged();}}
    if(!be.cargo.isEmpty()&&be.cargo.getFirst().progress>=20)exiting.add(be);
    if(!be.cargo.isEmpty()&&!be.moving&&!canExit(w,n,be,tick))be.status=4;
   }}
   boolean visual=be.powered&&(busy.contains(driver)||!occupied.contains(driver));if(be.getBlockState().getValue(ConveyorBlock.ACTIVE)!=visual&&tick%10==0)w.setBlock(be.getBlockPos(),be.getBlockState().setValue(ConveyorBlock.ACTIVE,visual),2);
   if(old!=be.powered||oldMoving!=be.moving||oldStatus!=be.status||!java.util.Objects.equals(oldDrive,be.drivePos))be.sync();
  }
  for(var be:exiting)exit(w,n,be,tick);
 }
 private ConveyorNetwork(){}
}
