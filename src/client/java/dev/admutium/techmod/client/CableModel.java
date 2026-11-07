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
        for (int i=0;i<54;i++) {
            Node n=Node.fromIndex(pos.getX(),pos.getY(),pos.getZ(),i);
            if (data.has(n)) {
                int variant=Math.floorMod(Objects.hash(n.x(),n.y(),n.z(),i),3);
                var connections=SurfaceTopology.connections(n,other -> {
                    CableSnapshot s=snapshot(level,new BlockPos(other.x(),other.y(),other.z()));
                    return s.has(other) || (other.u()==1 && other.v()==1 && s.holder(other.face()));
                });
                if (connections.isEmpty()) {
                    List<Vec> coil=SurfaceTopology.coil(n);
                    polyline(emitter,coil,origin,Node.RADIUS,variant);
                    cap(emitter,coil.getFirst(),coil.get(1).subtract(coil.getFirst()).scale(-1),origin,Node.RADIUS,3);
                    cap(emitter,coil.getLast(),coil.getLast().subtract(coil.get(coil.size()-2)),origin,Node.RADIUS,3);
                } else {
                    for (var link:connections) {
                        List<Vec> half=half(link.path());
                        polyline(emitter,wobble(half,n),origin,Node.RADIUS,variant);
                    }
                    if (connections.size()==1) {
                        Vec direction=connections.getFirst().path().get(1).subtract(n.point()).normalized();
                        cap(emitter,n.point(),direction.scale(-1),origin,Node.RADIUS,3);
                    }
                }
                // Future machine blocks opt into CablePort. The sleeve appears only at a real compatible port.
                for (Direction d:Direction.values()) {
                    if (axis(d)==n.face().axis) continue;
                    BlockPos machine=pos.relative(d);
                    if (level.getBlockState(machine).getBlock() instanceof CablePort port && port.acceptsCable(level,machine,d.getOpposite())) {
                        Vec end=n.point().with(axis(d),origin.coordinate(axis(d))+(d.getAxisDirection()==Direction.AxisDirection.POSITIVE ? .99 : .01));
                        if (end.subtract(n.point()).length()<.3) {
                            tube(emitter,n.point(),end,origin,Node.RADIUS,variant);
                            Vec sleeve=end.add(n.point().subtract(end).normalized().scale(.13));
                            tube(emitter,sleeve,end,origin,.068,4);
                            cap(emitter,end,end.subtract(sleeve),origin,.068,4);
                        }
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
                polyline(emitter,half(link.path()),origin,Node.RADIUS,0);
        }
        for (CableSpan span:data.spans()) {
            List<Vec> points=span.points();
            for (int i=1;i<points.size();i++) {
                Vec a=points.get(i-1), b=points.get(i), mid=a.add(b).scale(.5);
                if (BlockPos.containing(mid.x(),mid.y(),mid.z()).equals(pos))
                    tube(emitter,a,b,origin,Node.RADIUS,Math.floorMod(span.id().hashCode(),3));
            }
        }
    }
    private static int axis(Direction d) { return d.getStepX()!=0 ? 0 : d.getStepY()!=0 ? 1 : 2; }
    private static List<Vec> half(List<Vec> points) {
        double total=0;
        for (int i=1;i<points.size();i++) total+=points.get(i).subtract(points.get(i-1)).length();
        List<Vec> result=new ArrayList<>(); result.add(points.getFirst());
        double remaining=total/2;
        for (int i=1;i<points.size();i++) {
            Vec a=points.get(i-1), b=points.get(i); double length=b.subtract(a).length();
            if (remaining<=length) { result.add(a.add(b.subtract(a).scale(remaining/length))); break; }
            result.add(b); remaining-=length;
        }
        return result;
    }
    private static List<Vec> wobble(List<Vec> points,Node node) {
        List<Vec> result=new ArrayList<>();
        for (int i=1;i<points.size();i++) {
            Vec a=points.get(i-1),b=points.get(i);
            Vec tangent=b.subtract(a).normalized();
            Vec sideways=tangent.cross(node.face().normal()).normalized();
            if (i==1) result.add(a);
            double amount=((Objects.hash(node.x(),node.y(),node.z(),node.index())&1)==0 ? 1 : -1)*.009;
            result.add(a.add(b).scale(.5).add(sideways.scale(amount)));
            result.add(b);
        }
        return result;
    }
    private void polyline(QuadEmitter out,List<Vec> points,Vec origin,double radius,int material) {
        for (int i=1;i<points.size();i++) tube(out,points.get(i-1),points.get(i),origin,radius,material);
    }
    private static Vec[] frame(Vec direction) {
        Vec tangent=direction.normalized();
        Vec u=tangent.cross(Math.abs(tangent.y())<.9 ? new Vec(0,1,0) : new Vec(1,0,0)).normalized();
        return new Vec[]{u,tangent.cross(u).normalized()};
    }
    private void tube(QuadEmitter out,Vec a,Vec b,Vec origin,double radius,int material) {
        if (b.subtract(a).length()<1e-7) return;
        Vec[] frame=frame(b.subtract(a));
        for (int side=0;side<8;side++) {
            double angle=side*Math.PI/4,next=(side+1)*Math.PI/4;
            Vec p=frame[0].scale(Math.cos(angle)*radius).add(frame[1].scale(Math.sin(angle)*radius));
            Vec q=frame[0].scale(Math.cos(next)*radius).add(frame[1].scale(Math.sin(next)*radius));
            quad(out,new Vec[]{a.add(p),a.add(q),b.add(q),b.add(p)},origin,material);
        }
    }
    private void cap(QuadEmitter out,Vec center,Vec direction,Vec origin,double radius,int material) {
        Vec[] frame=frame(direction);
        // The end texture is a rubber ring with a stranded copper core.
        Vec u=frame[0].scale(radius),v=frame[1].scale(radius);
        quad(out,new Vec[]{center.subtract(u).subtract(v),center.add(u).subtract(v),
                center.add(u).add(v),center.subtract(u).add(v)},origin,material);
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
