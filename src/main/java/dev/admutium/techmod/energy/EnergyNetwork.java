package dev.admutium.techmod.energy;
import dev.admutium.techmod.cable.*;
import dev.admutium.techmod.machine.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import java.util.*;
/** Routes are rebuilt only after topology/chunk changes. Transfer never creates a new graph per tick. */
public final class EnergyNetwork {
 private EnergyNetwork(){}
 private static final Map<ServerLevel,Network> WORLDS=new HashMap<>();
 private static final class Network {final Map<BlockPos,MachineBlockEntity> machines=new HashMap<>();final Map<BlockPos,List<Route>> routes=new HashMap<>();boolean dirty=true;int turn;}
 private record Route(MachineBlockEntity target,double length,List<Node> nodes){}
 private record Visit(Node node,double length){}
 public static void clearAll(){WORLDS.clear();}
 public static void clear(ServerLevel w){WORLDS.remove(w);}
 public static void invalidate(Level w){if(w instanceof ServerLevel s){var n=WORLDS.get(s);if(n!=null)n.dirty=true;}}
 public static void register(ServerLevel w,MachineBlockEntity m){var n=WORLDS.computeIfAbsent(w,k->new Network());if(n.machines.put(m.getBlockPos(),m)!=m)n.dirty=true;}
 private static MachineBlock block(MachineBlockEntity m){return (MachineBlock)m.getBlockState().getBlock();}
 public static void tick(ServerLevel w){
  var n=WORLDS.get(w);if(n==null)return;
  if(n.machines.entrySet().removeIf(e->e.getValue().isRemoved() || !w.hasChunkAt(e.getKey()) || w.getBlockEntity(e.getKey())!=e.getValue()))n.dirty=true;
  var list=new ArrayList<>(n.machines.values());list.sort(Comparator.comparingLong(m->m.getBlockPos().asLong()));
  for(var m:list)m.beginTick();
  if(n.dirty){n.routes.clear();for(var m:list)if(block(m).output(m.getBlockState())!=null)n.routes.put(m.getBlockPos(),routes(w,n,m));n.dirty=false;}
  var wires=new HashMap<Node,Long>();
  for(int type=0;type<2;type++)for(int j=0;j<list.size();j++){
   var source=list.get((j+n.turn)%list.size());
   if((source.kind==MachineKind.GENERATOR?0:1)!=type || source.processes())continue;
   long budget=Math.min(source.portLimit(),source.stored());
   var all=n.routes.getOrDefault(source.getBlockPos(),List.of());
   for(int priority=0;priority<2 && budget>0;priority++){
    var eligible=new ArrayList<Route>();for(var r:all)if(!r.target.isRemoved() && r.target.capacity()>r.target.stored()
     && (r.target.kind==MachineKind.CABINET?1:0)==priority && (source.kind==MachineKind.GENERATOR || r.target.kind!=MachineKind.CABINET))eligible.add(r);
    if(!eligible.isEmpty())Collections.rotate(eligible,n.turn%eligible.size());
    for(int round=0;round<2 && budget>0;round++)for(int i=0;i<eligible.size() && budget>0;i++){
     var r=eligible.get(i);var sink=r.target;long allowed=Math.max(1,budget/(eligible.size()-i));
     allowed=Math.min(allowed,sink.portLimit()-sink.incoming);
     for(var node:r.nodes)allowed=Math.min(allowed,9_000-wires.getOrDefault(node,0L));
     double efficiency=Math.max(0,1-r.length*.001);if(efficiency<=0 || allowed<=0)continue;
     long room=sink.capacity()-sink.stored();long sent=Math.min(allowed,(long)Math.ceil(room/efficiency));
     long delivered=Energy.delivered(sent,r.length);if(delivered<=0)continue;
     long accepted=sink.receive(Math.min(room,delivered));if(accepted<=0)continue;
     source.extract(sent);budget-=sent;source.outgoing+=sent;source.loss+=sent-accepted;sink.incoming+=accepted;
     for(var node:r.nodes)wires.merge(node,sent,Long::sum);
     source.setChanged();sink.setChanged();
    }
   }
  }
  for(var m:list)m.finishTick();n.turn=(n.turn+1)&0x7fffffff;
 }
 private static boolean present(ServerLevel w,Node node){var d=CableWorld.data(w,CableWorld.pos(node)).centered();return d.has(node)||d.holder(node.face());}
 private static List<Route> routes(ServerLevel w,Network net,MachineBlockEntity source){
  Direction output=block(source).output(source.getBlockState());if(output==null)return List.of();
  BlockPos start=source.getBlockPos().relative(output);var found=new HashMap<MachineBlockEntity,Route>();
  var direct=net.machines.get(start);
  if(direct!=null && block(direct).input(direct.getBlockState())==output.getOpposite())found.put(direct,new Route(direct,0,List.of()));
  var distance=new HashMap<Node,Double>();var parents=new HashMap<Node,Node>();
  PriorityQueue<Visit> queue=new PriorityQueue<>(Comparator.comparingDouble(Visit::length));
  Vec origin=port(source.getBlockPos(),output);
  for(Face f:Face.values())if(f.axis!=Face.valueOf(output.name()).axis || f==Face.valueOf(output.getOpposite().name())){
   Node node=CableLayout.placement(start.getX(),start.getY(),start.getZ(),f);if(present(w,node)){double d=terminalDistance(node.point(),origin);distance.put(node,d);queue.add(new Visit(node,d));}
  }
  int explored=0;
  while(!queue.isEmpty() && explored++<32_768){
   Visit visit=queue.poll();Node node=visit.node;if(visit.length>distance.getOrDefault(node,Double.MAX_VALUE)+1e-8)continue;
   BlockPos p=CableWorld.pos(node);
   for(Direction dir:Direction.values()){
    if(dir.getAxis().ordinal()==node.face().axis && Face.valueOf(dir.name())!=node.face())continue;
    var target=net.machines.get(p.relative(dir));if(target==null || target==source || block(target).input(target.getBlockState())!=dir.getOpposite())continue;
    double d=visit.length+terminalDistance(node.point(),port(target.getBlockPos(),dir.getOpposite()));
    if(!found.containsKey(target)||d<found.get(target).length){var path=new ArrayList<Node>();for(Node at=node;at!=null;at=parents.get(at))path.add(at);found.put(target,new Route(target,d,List.copyOf(path)));}
   }
   for(var link:SurfaceTopology.connections(node,other->present(w,other))){double d=0;for(int i=1;i<link.path().size();i++)d+=link.path().get(i).subtract(link.path().get(i-1)).length();relax(node,link.target(),visit.length+d,distance,parents,queue);}
   var data=CableWorld.data(w,p);
   if(data.holder(node.face()))for(var span:data.spans()){
    Node other=span.start().equals(node)?span.end():span.end().equals(node)?span.start():null;
    if(other!=null && present(w,other))relax(node,other,visit.length+span.length(),distance,parents,queue);
   }
  }
  return List.copyOf(found.values());
 }
 private static void relax(Node from,Node to,double d,Map<Node,Double> dist,Map<Node,Node> parents,PriorityQueue<Visit> queue){if(d+1e-8<dist.getOrDefault(to,Double.MAX_VALUE)){dist.put(to,d);parents.put(to,from);queue.add(new Visit(to,d));}}
 private static double terminalDistance(Vec a,Vec b){var d=a.subtract(b);return Math.abs(d.x())+Math.abs(d.y())+Math.abs(d.z());}
 private static Vec port(BlockPos p,Direction d){return new Vec(p.getX()+.5+d.getStepX()*.5,p.getY()+.5+d.getStepY()*.5,p.getZ()+.5+d.getStepZ()*.5);}
}
