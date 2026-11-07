package dev.admutium.techmod.client;

import com.mojang.serialization.MapCodec;
import dev.admutium.techmod.TechMod;
import dev.admutium.techmod.cable.*;
import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import java.util.*;
import java.util.function.Predicate;

/** Chunk-meshed geometry, including suspended segments assigned to their actual cells. */
public final class CableModel implements BlockStateModel {
    private final Material.Baked[] materials;
    private CableModel(Material.Baked[] materials) { this.materials=materials; }
    private static CableSnapshot snapshot(BlockAndTintGetter level, BlockPos p) {
        Object data=((FabricBlockGetter)level).getBlockEntityRenderData(p);
        return data instanceof CableSnapshot s ? s : CableSnapshot.EMPTY;
    }
    @Override public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                                    RandomSource random, Predicate<@Nullable Direction> cullTest) {
        CableSnapshot data=snapshot(level,pos);
        Vec origin=new Vec(pos.getX(),pos.getY(),pos.getZ());
        for (Face face:Face.values()) {
            Node n=CableLayout.placement(pos.getX(),pos.getY(),pos.getZ(),face);
            int i=n.index();
            if (data.has(n)) {
                int variant=Math.floorMod(Objects.hash(n.x(),n.y(),n.z(),i),3);
                var connections=SurfaceTopology.connections(n,other -> {
                    CableSnapshot s=snapshot(level,new BlockPos(other.x(),other.y(),other.z()));
                    return s.has(other) || (other.u()==1 && other.v()==1 && s.holder(other.face()));
                });
                if (connections.isEmpty()) {
                    List<Vec> coil=SurfaceTopology.coil(n);
                    var rings=TubeGeometry.rings(coil,Node.RADIUS);
                    tubeRings(emitter,rings,origin,variant,0,rings.size()-1);
                    ringCap(emitter,rings.getFirst(),origin,3,true);
                    ringCap(emitter,rings.getLast(),origin,3,false);
                } else {
                    for (var link:connections) connection(emitter,n,link,origin,variant,connections.size()==1);
                    if (connections.size()>1) joint(emitter,n.point(),origin,variant);
                }
                BlockPos support=pos.relative(Direction.valueOf(n.face().name()));
                if(level.getBlockState(support).getBlock() instanceof CablePort port && port.acceptsCable(level,support,Direction.valueOf(n.face().opposite().name()))) {
                    Vec socket=n.point().add(n.face().normal().scale(Node.HEIGHT));
                    tube(emitter,n.point(),socket,origin,.063,4);
                }
                // Compatible ports receive a surface-following lead and a visible terminal sleeve.
                for (Direction d:Direction.values()) {
                    if (axis(d)==n.face().axis) continue;
                    BlockPos machine=pos.relative(d);
                    if (level.getBlockState(machine).getBlock() instanceof CablePort port && port.acceptsCable(level,machine,d.getOpposite())) {
                        Vec end=new Vec(machine.getX()+.5-d.getStepX()*.5,machine.getY()+.5-d.getStepY()*.5,machine.getZ()+.5-d.getStepZ()*.5);
                        Vec wall=end.with(axis(d),end.coordinate(axis(d))-(d.getStepX()+d.getStepY()+d.getStepZ())*Node.HEIGHT);
                        Vec corner=wall.with(n.face().axis,n.point().coordinate(n.face().axis));
                        List<Vec> path=new ArrayList<>();path.add(n.point());
                        if(corner.subtract(path.getLast()).length()>1e-7)path.add(corner);
                        if(wall.subtract(path.getLast()).length()>1e-7)path.add(wall);
                        if(end.subtract(path.getLast()).length()>1e-7)path.add(end);
                        var rings=TubeGeometry.rings(path,Node.RADIUS);
                        tubeRings(emitter,rings,origin,variant,0,rings.size()-1);
                        Vec sleeve=end.subtract(new Vec(d.getStepX(),d.getStepY(),d.getStepZ()).scale(.13));
                        tube(emitter,sleeve,end,origin,.068,4);
                        cap(emitter,end,end.subtract(sleeve),origin,.068,4);
                    }
                }
            }
        }
        for (Face f:Face.values()) if (data.holder(f)) {
            Node center=new Node(pos.getX(),pos.getY(),pos.getZ(),f,1,1);
            Vec base=center.point().add(f.normal().scale(.04));
            Vec top=center.point().subtract(f.normal().scale(.08));
            tube(emitter,base,top,origin,.11,4);
            cap(emitter,top,f.normal().scale(-1),origin,.11,4);
            // Four mounting screws on the plate.
            for (int u:new int[]{-1,1}) for (int v:new int[]{-1,1}) {
                Vec screw=top.with(f.uAxis(),top.coordinate(f.uAxis())+u*.065)
                        .with(f.vAxis(),top.coordinate(f.vAxis())+v*.065);
                cap(emitter,screw.subtract(f.normal().scale(.001)),f.normal().scale(-1),origin,.012,0);
            }
            // Draw the holder's half of any surface connections.
            for (var link:SurfaceTopology.connections(center,n -> snapshot(level,new BlockPos(n.x(),n.y(),n.z())).has(n)))
                connection(emitter,center,link,origin,0,false);
        }
        for (CableSpan span:data.spans()) {
            var rings=TubeGeometry.rings(span.points(),Node.RADIUS);
            int variant=Math.floorMod(span.id().hashCode(),3);
            for (int i=1;i<rings.size();i++) {
                Vec mid=rings.get(i-1).center().add(rings.get(i).center()).scale(.5);
                if (BlockPos.containing(mid.x(),mid.y(),mid.z()).equals(pos))
                    tubeRings(emitter,rings,origin,variant,i-1,i);
            }
        }
    }
    private static int axis(Direction d) { return d.getStepX()!=0 ? 0 : d.getStepY()!=0 ? 1 : 2; }
    private void connection(QuadEmitter out,Node n,SurfaceTopology.Connection link,Vec origin,int material,boolean exposed) {
        var path=TubeGeometry.connection(n,link);
        var rings=TubeGeometry.rings(path.points(),Node.RADIUS);
        int from=path.firstHalf()?0:path.split(),to=path.firstHalf()?path.split():rings.size()-1;
        tubeRings(out,rings,origin,material,from,to);
        if (exposed) ringCap(out,path.firstHalf()?rings.getFirst():rings.getLast(),origin,3,path.firstHalf());
    }
    private void tubeRings(QuadEmitter out,List<TubeGeometry.Ring> rings,Vec origin,int material,int from,int to) {
        for (int i=from+1;i<=to;i++) {
            var a=rings.get(i-1).vertices();var b=rings.get(i).vertices();
            for (int side=0;side<TubeGeometry.SIDES;side++) {
                int next=(side+1)%TubeGeometry.SIDES;
                quad(out,new Vec[]{a.get(side),a.get(next),b.get(next),b.get(side)},origin,material);
            }
        }
    }
    private void ringCap(QuadEmitter out,TubeGeometry.Ring ring,Vec origin,int material,boolean reverse) {
        for(int side=0;side<TubeGeometry.SIDES;side++) {
            int next=(side+1)%TubeGeometry.SIDES;
            int a=reverse?next:side,b=reverse?side:next;
            Vec[] points={ring.center(),ring.vertices().get(a),ring.vertices().get(b),ring.vertices().get(b)};
            double angleA=a*Math.PI*2/TubeGeometry.SIDES,angleB=b*Math.PI*2/TubeGeometry.SIDES;
            for(int i=0;i<4;i++) {
                Vec p=points[i].subtract(origin);
                out.pos(i,(float)p.x(),(float)p.y(),(float)p.z()).color(i,0xFFFFFFFF);
            }
            out.uv(0,.5f,.5f);
            out.uv(1,(float)(.5+.5*Math.cos(angleA)),(float)(.5+.5*Math.sin(angleA)));
            out.uv(2,(float)(.5+.5*Math.cos(angleB)),(float)(.5+.5*Math.sin(angleB)));
            out.uv(3,(float)(.5+.5*Math.cos(angleB)),(float)(.5+.5*Math.sin(angleB)));
            out.materialBake(materials[material],0).emit();
        }
    }
    private void joint(QuadEmitter out,Vec center,Vec origin,int material) {
        double r=Node.RADIUS*1.055;
        List<List<Vec>> latitudes=new ArrayList<>();
        for(int latitude=-1;latitude<=1;latitude++) {
            double angle=latitude*Math.PI/4;
            List<Vec> ring=new ArrayList<>();
            for(int side=0;side<8;side++) {
                double around=side*Math.PI/4;
                ring.add(center.add(new Vec(Math.cos(around)*Math.cos(angle)*r,Math.sin(angle)*r,-Math.sin(around)*Math.cos(angle)*r)));
            }
            latitudes.add(ring);
        }
        for(int row=1;row<3;row++) for(int side=0;side<8;side++) {
            int next=(side+1)%8;
            quad(out,new Vec[]{latitudes.get(row-1).get(side),latitudes.get(row-1).get(next),
                    latitudes.get(row).get(next),latitudes.get(row).get(side)},origin,material);
        }
        Vec top=center.add(new Vec(0,r,0)),bottom=center.add(new Vec(0,-r,0));
        for(int side=0;side<8;side++) {
            int next=(side+1)%8;
            quad(out,new Vec[]{bottom,latitudes.getFirst().get(next),latitudes.getFirst().get(side),latitudes.getFirst().get(side)},origin,material);
            quad(out,new Vec[]{top,latitudes.getLast().get(side),latitudes.getLast().get(next),latitudes.getLast().get(next)},origin,material);
        }
    }
    private void tube(QuadEmitter out,Vec a,Vec b,Vec origin,double radius,int material) {
        if (b.subtract(a).length()<1e-7) return;
        var rings=TubeGeometry.rings(List.of(a,b),radius);
        tubeRings(out,rings,origin,material,0,1);
    }
    private void cap(QuadEmitter out,Vec center,Vec direction,Vec origin,double radius,int material) {
        var ring=TubeGeometry.rings(List.of(center,center.add(direction.normalized())),radius).getFirst();
        ringCap(out,ring,origin,material,false);
    }
    private void quad(QuadEmitter out,Vec[] points,Vec origin,int material) {
        for (int i=0;i<4;i++) {
            Vec p=points[i].subtract(origin);
            out.pos(i,(float)p.x(),(float)p.y(),(float)p.z());
            out.uv(i,i==1 || i==2 ? 1:0,i>=2 ? 1:0);
            out.color(i,0xFFFFFFFF);
        }
        out.materialBake(materials[material],0).emit();
    }
    @Override public @Nullable Object createGeometryKey(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random) { return null; }
    @Override public void collectParts(RandomSource random,List<BlockStateModelPart> parts) {}
    @Override public Material.Baked particleMaterial() { return materials[0]; }
    @Override public int materialFlags() { return 0; }

    public record Unbaked() implements CustomUnbakedBlockStateModel, ModelDebugName {
        public static final MapCodec<Unbaked> CODEC=MapCodec.unit(new Unbaked());
        @Override public MapCodec<? extends CustomUnbakedBlockStateModel> codec() { return CODEC; }
        @Override public void resolveDependencies(Resolver resolver) {}
        @Override public BlockStateModel bake(ModelBaker baker) {
            String[] names={"rubber","rubber_ribbed","rubber_marked","copper_end","holder"};
            Material.Baked[] materials=new Material.Baked[names.length];
            for (int i=0;i<names.length;i++) materials[i]=baker.materials().get(new Material(TechMod.id("block/"+names[i])),this);
            return new CableModel(materials);
        }
        @Override public String debugName() { return "TechMod cable geometry"; }
    }
}
